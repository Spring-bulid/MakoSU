---
title: 安装与更新
description: 安全安装、更新 MakoSU Manager 与正式 KMI 模块。
---

# 安装与更新

安装 APK 本身不会让设备获得 Root。MakoSU Manager 必须与已经正确集成 MakoSU/KernelSU 的内核，或与完全匹配的正式 LKM 一起使用。

## 安装前检查

开始前确认以下条件：

- Bootloader 已解锁，并清楚解锁可能触发数据清除。
- 已备份当前槽位的原始启动镜像，能够进入 Fastboot 或 Recovery。
- 设备属于[正式 KMI 范围](/guide/compatibility)，或使用设备维护者明确提供的适配内核。
- 下载的是 MakoSU 正式 Release，Manager 包名为 `com.makosu.manager`。
- 已记录当前活动槽位和设备使用的启动分区，不凭 Android 版本猜测分区。

::: warning 不要混用发布身份
Manager 包名、APK Release 证书、内核预期证书大小与证书哈希属于同一发布契约。使用其他签名重打包的 APK 时，即使界面名称相同，内核也可能拒绝把它识别为管理器。
:::

## 校验下载文件

发布 ZIP 中应包含 `SHA256SUMS.txt`。在刷写前核对 APK、ZIP 或 LKM 文件的 SHA-256，校验值必须与发布页一致。

::: code-group

```powershell [Windows PowerShell]
Get-FileHash .\MakoSU_*.apk -Algorithm SHA256
Get-FileHash .\MakoSU_*.zip -Algorithm SHA256
```

```bash [Linux / macOS]
sha256sum MakoSU_*.apk MakoSU_*.zip
```

:::

校验不一致时不要继续安装，也不要从聊天群转存文件推断版本是否可信。

## 首次安装

1. 安装与当前发布配套的 MakoSU Manager。
2. 打开首页，查看管理器是否识别当前内核和运行模式。
3. 如果设备已经运行适配内核，先完成授权与基础状态检查，不要重复刷写。
4. 如果需要安装 LKM，让 Manager 自动读取完整 Android KMI 标记。
5. 只有在自动结果与设备内核、厂商 ABI 和维护者说明完全一致时，才手动选择 KMI。
6. 执行安装或镜像修补前，再次确认目标分区、槽位和原始镜像备份。
7. 安装成功后重启，检查 Manager 状态、授权弹窗与模块页面是否正常。

## 选择 LKM

正式发布只提供以下 KMI：

`android12-5.10`、`android13-5.10`、`android13-5.15`、`android14-5.15`、`android14-6.1`、`android15-6.6`、`android16-6.12`。

不要因为设备同为 `5.10` 或 `5.15` 就强制加载。Android KMI 世代、厂商 ABI、导出符号与内核配置都必须匹配。Manager 没有给出可用项时，应停止操作并查看[兼容性说明](/guide/compatibility)。

## 更新

更新 Manager 或内核前：

1. 阅读 Release 说明，确认 Manager、KMI 与签名身份是否同时更新。
2. 备份原始镜像、当前可用镜像以及重要的 SuSFS 配置。
3. 先安装同一 Release 的 Manager，再使用该 Release 提供的 KMI 或内核产物。
4. 不要把新 Manager 与旧下游的 `kernelsu.ko` 混用。
5. A/B 设备执行"安装到未使用槽位"时，只能用于 OTA 完成后、首次重启前的明确场景，并核对目标槽位。

## 安装后检查

- 首页显示已安装，并能看到正确的内核版本和 LKM/GKI 模式。
- Root 授权能够正常弹出，应用列表与授权状态可以刷新。
- 模块页没有安全模式或冲突提示。
- SuSFS 页面显示的能力与设备内核实际启用项一致。
- 重启一次后状态仍然存在，没有进入回滚或异常槽位。

若 Manager 显示"未安装"或刷写后无法启动，请立即转到[故障排查与救砖](/guide/troubleshooting)，不要连续尝试不同 LKM。
