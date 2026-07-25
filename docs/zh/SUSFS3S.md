# SUSFS3S — LKM 模式下的 SuSFS 措施

SUSFS3S 是 MakoSU 自研的 SuSFS 功能子集，让 **LKM（可加载内核模块）模式**的设备也能使用 SuSFS 的核心隐藏措施，无需给内核源码打 SuSFS 补丁。

## 为什么需要它

上游 SuSFS（susfs4ksu）以**内核源码补丁**形式分发，只有 GKI 集成编译（把 KernelSU 编进内核镜像）的设备才能使用。LKM 模式下 KernelSU 只是一个 `kernelsu.ko` 模块，无法应用这些补丁，因此 SuSFS 功能（路径隐藏、stat 伪装等）在 LKM 设备上一直是空白。

SUSFS3S 直接在 MakoSU 驱动内部实现这些措施，复用驱动已有的 syscall hook 框架，**任何能加载 MakoSU LKM 的设备都能用**。

## 功能列表（v1）

| 功能 | 说明 | 豁免 |
|---|---|---|
| **sus_path（路径隐藏）** | 对列表中的路径，`openat` / `newfstatat` / `faccessat` / `statx` 一律返回 `-ENOENT`（文件不存在） | root（uid 0）和已加冕管理器 |
| **sus_kstat（stat 伪装）** | 对列表中的路径，`newfstatat` 返回的 stat 结构体被完整模板覆盖（mode/uid/gid/size/时间戳等） | 不豁免（与上游 SuSFS 一致） |
| **spoof_uname（uname 伪装）** | 伪装内核版本字符串，由驱动已有的 uts_spoof 承担 | — |

已具备的互补措施：`uts_spoof`（uname 伪装）、`selinux_hide`（SELinux 相关隐藏）。

## 架构

```
管理器 SuSFS 界面（设置 → 模块 → SuSFS 配置）
        │  ksud susfs <命令>（原有协议，界面零改动）
        ▼
ksud susfs.rs —— 探测真 SuSFS 内核
        │  无真 SuSFS → 透明回退
        ▼
ksud susfs3s 封装（ksucalls.rs）
        │  supercall 'K',107（KSU_IOCTL_SUSFS3S）
        ▼
内核 feature/susfs3s.c
        │  syscall hook（hook/syscall_hook_manager.c）
        ▼
openat / newfstatat / faccessat / statx 过滤
```

### 透明回退

`ksud susfs` 命令在运行时探测：

1. 存在真 SuSFS 内核 → 一切走原有协议，行为不变；
2. 不存在真 SuSFS 但内核支持 SUSFS3S → 自动转发到 SUSFS3S：
   - 版本号显示为 `SUSFS3S-1.0`
   - 特性报告：`SUS_PATH` / `SUS_KSTAT` / `SPOOF_UNAME`
   - `add_sus_path` → SUSFS3S 路径隐藏
   - `add_sus_kstat`（含 update / full_clone）→ 实时 stat 生成模板后注册伪装
   - `set_uname` → uts_spoof

因此**管理器现有 SuSFS 界面无需任何改动**，两种内核下都能工作。

## 使用方法

### 管理器界面

**设置 → 模块 → SuSFS 配置**（内核支持时入口自动出现，不依赖 KPM）

- 特性页：查看 SUS_PATH / SUS_KSTAT / SPOOF_UNAME 三项可用
- SUS 路径：添加/删除要隐藏的路径（须为规范绝对路径）
- SUS KSTAT：添加/删除要伪装 stat 的路径
- uname 伪装：设置自定义内核版本字符串

### 命令行（root）

```sh
ksud susfs3s add-sus-path /sdcard/some_dir        # 添加隐藏路径
ksud susfs3s del-sus-path /sdcard/some_dir        # 移除隐藏路径
ksud susfs3s add-sus-kstat /data/x <ino> <dev> <nlink> <mode> <uid> <gid> <rdev> <size> <blksize> <blocks> <atime> <mtime> <ctime>
ksud susfs3s del-sus-kstat /data/x                # 移除 stat 伪装
ksud susfs3s wipe                                 # 清空两张表
```

原有 `ksud susfs ...` 命令在 LKM 设备上自动回退，无需直接使用 `susfs3s` 子命令。

## 内核接口（supercall）

命令号：`KSU_IOCTL_SUSFS3S = _IOC(_IOC_READ | _IOC_WRITE, 'K', 107, 0)`，权限要求 `manager_or_root`。

```c
struct ksu_susfs3s_cmd {
    __u8  op;            /* 0=ADD_SUS_PATH 1=DEL_SUS_PATH 2=ADD_SUS_KSTAT 3=DEL_SUS_KSTAT 4=WIPE */
    __u8  pad[3];
    char  path[256];     /* 规范绝对路径 */
    __u64 st_ino, st_size, st_blksize, st_blocks;
    __s64 st_atime, st_mtime, st_ctime;
    __u32 st_mode, st_uid, st_gid, st_dev, st_nlink, st_rdev;
};
```

## 实现原理

- **路径隐藏**：在 syscall 入口复制用户路径字符串，绝对路径直接比对；相对路径按 dirfd 解析（`AT_FDCWD` 取 `current->fs->pwd`，其他 fd 取 `f_path`）后比对。命中即返回 `-ENOENT`。列表为 64 槽静态数组，spinlock 保护。
- **stat 伪装**：`newfstatat` 原调用成功后，按原始请求路径查模板表，读回用户 stat 缓冲、逐字段覆盖、写回用户态。
- **顺序**：sus_path 过滤在 sucompat 重定向**之前**执行，被隐藏的路径不会触发 su→ksud 重定向。
- **版本**：驱动版本号由仓库根 `version.txt` 统一注入（管理器 / ksud / 内核三端一致）。

## 已知限制

- 路径注册项必须是**规范绝对路径**：不折叠 `.` / `..` / 重复斜杠，绕过形式（如 `/a/./b`）不匹配。
- 相对路径按调用者的 cwd/root namespace 视角解析，chroot 或独立挂载命名空间内视角可能不同。
- `statx` 只做 sus_path 隐藏，不做 kstat 伪装。
- kstat 模板为全字段覆盖，下发方必须先真实 stat 再改需要的字段。
- `add-sus-path-loop`（循环挂载路径）在 SUSFS3S 中无对应能力，回退下为 no-op（与真 SuSFS 缺失时行为一致）。

## 构建

内核模块（WSL，7 个 KMI 目标）：

```sh
cd kernel && bash build-all.sh
# android16-6.12 需单独构建（build-all.sh 头部注释有说明）：
rm -rf out/android16-6.12 && ddk build android16-6.12 -e CONFIG_KSU=m -- PAHOLE=true
```

ksud 与管理器：

```sh
# WSL 内三架构 cargo 构建（需 ANDROID_NDK_HOME 指向 Linux NDK r29）
cargo build --target aarch64-linux-android --release --manifest-path userspace/ksud/Cargo.toml
# 管理器 APK
cd manager && ./gradlew.bat :app:assembleRelease
```

`.ko` 内嵌于 ksud（RustEmbed），ksud 内嵌于管理器 `jniLibs`，随 APK 一体分发。
