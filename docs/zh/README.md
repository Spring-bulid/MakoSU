# MakoSU

<img align="right" src="../MakoSU-mini.png" width="220px" alt="MakoSU logo">

[English](../README.md) | **简体中文** | [繁體中文](../zh-TW/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Русский](../ru/README.md) | [Türkçe](../tr/README.md)

MakoSU 是 [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) 的下游项目。本仓库主要维护管理器、随版本发布的 KMI 模块、SuSFS 用户空间功能和相关构建脚本。

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-orange.svg?logo=gnu)](../../LICENSE)
[![Manager](https://img.shields.io/badge/Manager-Android%208.0%2B-3DDC84.svg?logo=android)](#兼容范围)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f81f7.svg)](#内置-kmi)
[![SuSFS](https://img.shields.io/badge/SuSFS-transactional-4c8bf5.svg)](#susfs)

> [!WARNING]
> MakoSU 会修改启动镜像或加载内核模块。错误的内核、LKM、签名身份或刷写目标可能导致设备无法启动。操作前必须备份原始启动镜像，并确认具备可用的救砖方式。

## 主要特性

- 基于内核的 `su`、授权管理和 App Profile。
- 支持 LKM 安装、启动镜像修补和 KMI 自动匹配。
- 管理器包名为 `com.makosu.manager`。
- 支持 KPM、模块管理、内核刷写和相关维护工具。
- 支持 Material 与 Miuix 界面及主题切换。
- 提供 SuSFS 配置、路径、映射、Kstat、uname 和自动启动管理。
- 发布构建强制使用 Release 签名；缺少签名配置时构建会失败。
- Manager 内置 `arm64-v8a`、`armeabi-v7a` 和 `x86_64` 用户空间组件。

## SuSFS

当前 SuSFS 用户空间实现包括：

- 配置文件使用跨进程锁保护，防止并发更新互相覆盖。
- 配置通过临时文件、`fsync` 和原子替换提交，降低异常断电造成的损坏风险。
- 二进制配置解析会拒绝截断数据、重复键、超限字段和尾随数据。
- 管理器通过一次 root 调用读取完整配置，避免连续启动大量命令造成界面卡顿。
- 自动启动模块采用暂存、同步、切换和失败回滚流程，不再先删除可用模块。
- 启动脚本的存储等待具有超时上限，不再无限等待或固定休眠 45 秒。
- 备份恢复使用单次配置替换；模块更新失败时会尝试恢复旧配置和旧模块状态。
- 用户输入统一进行 Shell 转义，并拒绝无法被当前持久化格式安全表示的分隔符。

SuSFS 的实际能力仍由设备内核中启用的功能决定。管理器不能为未集成 SuSFS 的内核凭空增加内核功能。

## 兼容范围

当前正式发布范围为 GKI 2.0、内核 `5.10` 及以上版本。

- Android 管理器最低系统版本：Android 8.0（API 26）。
- 自动 KMI 识别要求内核版本字符串包含有效的 Android KMI 标记。
- 自定义内核必须由其维护者正确集成 MakoSU/KernelSU，并匹配管理器身份契约。
- Android 11 / 5.4（GKI 1.0）目前不在正式发布和内置 KMI 范围内。
- 5.4 相关实验源码与脚本不代表通用兼容，也不得当作正式发布模块使用。

## 内置 KMI

当前发布包包含以下 7 个 KMI 模块：

| Android 世代 | 内核版本 | KMI 名称 |
| --- | --- | --- |
| Android 12 | 5.10 | `android12-5.10` |
| Android 13 | 5.10 | `android13-5.10` |
| Android 13 | 5.15 | `android13-5.15` |
| Android 14 | 5.15 | `android14-5.15` |
| Android 14 | 6.1 | `android14-6.1` |
| Android 15 | 6.6 | `android15-6.6` |
| Android 16 | 6.12 | `android16-6.12` |

不要仅根据主版本号强行加载 LKM。厂商 ABI、符号、KMI 标记或内核配置不匹配时，应使用设备对应的内核构建产物。

## 安装

1. 获取 MakoSU 的签名 Release APK 和对应发布包。
2. 核对下载页提供的 SHA-256；发布 ZIP 内还包含 `SHA256SUMS.txt`。
3. 安装管理器并授予必要权限。
4. 让管理器检测当前 KMI，或在完全确认版本匹配后手动选择 LKM。
5. 修补启动镜像或执行直接安装前，备份当前槽位的原始镜像。
6. 刷写后保留原始镜像和可进入 Fastboot/Recovery 的恢复方案。

不要混用其他包名、其他证书签名的管理器或来源不明的 `kernelsu.ko`。

## 管理器身份契约

正式发布使用以下身份：

| 项目 | 值 |
| --- | --- |
| 应用包名 | `com.makosu.manager` |
| 证书 DER 大小 | `0x0549` |
| 证书 SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

管理器包名、内核预期证书大小、内核预期证书哈希和 APK Release 证书属于同一个发布契约。任何一项发生变化，都必须同时重建并检查所有受支持 KMI。

## 从源码构建

### 环境要求

- Git
- Rust stable 与 Android Rust targets
- JDK 17
- Android SDK、Build Tools 37 和 NDK `29.0.14206865`
- 构建 KMI 时需要 Docker

### 构建 KMI

在 Bash 环境中运行：

```bash
bash scripts/build-makosu-kmi.sh
```

默认输出目录为 `out/lkm`，并要求最终模块集合与上面的 7 个 KMI 完全一致。

### 构建 Rust 与 Manager

在 Windows PowerShell 中运行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-makosu-rust.ps1
Set-Location .\manager
.\gradlew.bat testDebugUnitTest assembleDebug
```

Rust 构建脚本会生成三个 ABI 的 `ksud`，并复制到 Manager 的 `jniLibs`。

### Release 签名

在 `manager/makosu-signing.properties` 中配置以下字段，或者通过 CI Secret 提供：

```properties
KEYSTORE_FILE=path/to/release.jks
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=your_alias
KEY_PASSWORD=your_key_password
```

不要提交密钥或密码。缺少任一字段时，Release 构建必须失败，不能回退到 Debug 签名。

```powershell
Set-Location .\manager
.\gradlew.bat assembleRelease
```

发布前使用 Android Build Tools 验证 APK：

```powershell
apksigner verify --verbose --print-certs .\app\build\outputs\apk\release\MakoSU_*-release.apk
```

## 质量要求

提交改动前至少执行与改动范围对应的检查：

```bash
cargo fmt --manifest-path userspace/ksud/Cargo.toml --check
cargo test --manifest-path userspace/ksud/Cargo.toml --lib
```

Rust 改动需要运行 Android 目标 Clippy，Shell 改动需要运行 ShellCheck，Manager 改动需要运行单元测试和 `assembleDebug`。正式发布前还要构建签名 APK、验证 v2 证书并检查 KMI 包内容。

详细维护约束参见 [`MAINTENANCE.md`](../../MAINTENANCE.md) 和 [`CONTRIBUTING.md`](../../CONTRIBUTING.md)。

## 视觉素材与商标

README 使用 `docs/MakoSU-mini.png`，高清原图保存在 `docs/MakoSU.png`。

项目中与《千恋＊万花》相关的角色形象、作品名称和视觉素材，其著作权、商标权及其他知识产权归 YUZUSOFT（柚子社）及相应权利人所有。MakoSU 是非官方维护项目，与 YUZUSOFT 不存在隶属、授权或合作关系。

代码许可证不会自动授予任何人使用、修改或再分发上述角色美术和品牌素材的权利。使用相关素材前，应自行取得权利人的许可并遵守适用法律及素材附带条款。

## 许可证

- `kernel` 目录中的内核代码遵循其文件声明及 GPL-2.0-only 要求。
- 仓库其他代码以根目录 [`LICENSE`](../../LICENSE) 和各文件中的 SPDX/许可证声明为准。
- 第三方项目、字体、图标、角色美术和商标仍受各自许可证及权利声明约束。

## 鸣谢

- [KernelSU](https://github.com/tiann/KernelSU)：内核级 Root 基础项目。
- [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra)：MakoSU 的直接上游。
- [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU)：部分维护方式参考。
- [MKSU](https://github.com/5ec1cff/KernelSU)：Magic Mount 等实现参考。
- [RKSU](https://github.com/rsuntk/KernelsU)：non-GKI 相关工作。
- [susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu)：SuSFS 内核补丁与用户空间方案。
- [KernelPatch](https://github.com/bmax121/KernelPatch)：KPM/APatch 相关基础。
- [Magisk](https://github.com/topjohnwu/Magisk)：Android Root 与模块生态的重要项目。

## 免责声明

本项目按现状提供，不保证适配所有设备、厂商内核或系统版本。使用者需要自行承担解锁、刷写、Root、数据丢失、保修失效和设备损坏等风险。
