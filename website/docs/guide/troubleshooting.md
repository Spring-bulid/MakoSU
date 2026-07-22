---
title: 故障排查与救砖
description: 排查 MakoSU 未识别、LKM 失败、启动异常与 SuSFS 问题。
---

# 故障排查与救砖

先区分"Manager 界面问题""内核或身份不匹配""刷写后无法启动"三类故障。不要在原因不明时连续刷入多个 LKM，这会丢失最后一个可用状态。

## Manager 显示未安装

最常见原因不是应用界面，而是 Manager 与内核没有建立有效身份：

1. 仅安装了 APK，当前内核并未集成 MakoSU/KernelSU。
2. Manager 包名不是 `com.makosu.manager`。
3. APK 使用了其他 Release 或 Debug 证书，证书哈希与内核预期不一致。
4. 内核仍在识别其他下游 Manager 的包名或证书。
5. KMI 模块未加载，或加载的是不同 Android KMI 世代的模块。

先确认 APK 来源、完整 KMI 和内核构建说明。不要通过修改应用显示名称解决身份问题；包名、证书大小、证书哈希与全部 KMI 必须一起更新。

## LKM 加载或安装失败

- Manager 没有自动给出匹配项时停止安装。
- 检查完整 KMI，不要把所有 `5.10` 或 `5.15` 当作同一模块。
- 保留安装日志，确认失败发生在校验、写入、加载还是重启阶段。
- 如果操作失败但设备仍在运行，不要立刻重启或换另一个模块；先恢复原状态。
- 发布包校验失败时重新从 Releases 下载，不使用转存文件。

## 刷写后无法启动

::: danger 优先恢复，不要继续试错
进入 Bootloader 或 Recovery，恢复刷写前备份的原始启动镜像。不要在故障镜像上继续叠加补丁。
:::

通用恢复顺序：

1. 确认设备仍能进入 Bootloader、Fastboot 或 Recovery。
2. 核对当前活动槽位，避免把备份刷到错误槽位。
3. 将原始镜像刷回当时修改的同一分区；不要默认所有设备都使用 `boot`。
4. 启动成功后保留故障镜像、安装日志和完整设备信息，再分析 KMI 或分区判断。
5. 若问题由普通 Root 模块引起且内核本身能启动，优先使用安全模式或 Recovery 禁用该模块，而不是重刷内核。

具体刷写命令与分区名称取决于设备。没有设备官方文档或可靠维护说明时，不在文档中提供可能误刷的通用命令。

## SuSFS 配置异常

1. 禁用自动启动，避免异常配置在每次启动时重复应用。
2. 使用 Manager 的备份恢复功能回到上一份可用配置。
3. 没有备份时逐类恢复默认值，每次只处理一类规则。
4. 检查内核报告的已启用功能，不要在未支持项上继续添加规则。
5. 恢复后重启并验证，再决定是否重新启用自动启动。

## Manager 闪退或界面异常

- 更新到同一发布渠道的最新 Manager，不混装不同签名 APK。
- 先强制停止并清理缓存，不要先清除应用数据或卸载，以免丢失诊断状态。
- 复现一次问题后立即保存 Logcat。
- 主题、备用图标或关于页面问题应附带界面模式、系统版本和复现步骤。

## 采集诊断信息

连接 ADB 后执行：

```bash
adb shell uname -a
adb shell uname -r
adb shell getprop ro.build.version.release
adb shell getprop ro.product.device
adb shell getprop ro.boot.slot_suffix
adb logcat -d > makosu-logcat.txt
```

如果设备仍有可用 Root，可额外保存与 MakoSU、KernelSU、LKM 或 SuSFS 相关的内核日志。提交问题时包含：

- 设备型号、ROM 与 Android 版本。
- 完整内核版本字符串和 KMI。
- MakoSU Release 版本与下载文件哈希。
- 安装模式、目标分区、活动槽位和明确复现步骤。
- 已脱敏的 Manager 日志、Logcat 与内核日志。

不要公开设备序列号、账号信息、密钥、完整应用列表或未经检查的系统数据。可在 [GitHub Issues](https://github.com/Spring-bulid/MakoSU/issues) 提交可复现问题。
