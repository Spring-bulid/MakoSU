---
title: KMI 兼容性
description: 判断 MakoSU 正式 KMI 是否与 Android 设备内核匹配。
---

# KMI 兼容性

KMI 不是"内核主版本相同即可通用"。MakoSU 的自动选择依赖内核版本字符串中的 Android KMI 标记，最终还受厂商 ABI、符号、配置和集成方式影响。

## 正式 KMI 矩阵

| Android 世代 | 内核版本 | KMI 名称         |
| ------------ | -------- | ---------------- |
| Android 12   | 5.10     | `android12-5.10` |
| Android 13   | 5.10     | `android13-5.10` |
| Android 13   | 5.15     | `android13-5.15` |
| Android 14   | 5.15     | `android14-5.15` |
| Android 14   | 6.1      | `android14-6.1`  |
| Android 15   | 6.6      | `android15-6.6`  |
| Android 16   | 6.12     | `android16-6.12` |

以上 7 组是当前发布包的完整 KMI 集合。发布包之外的文件不因名称相近而获得正式支持。

## 判断顺序

按以下顺序判断，任意一项不明确都应停止加载：

1. **集成方式**：设备使用正式 GKI/LKM，还是厂商或第三方维护者的内建内核。
2. **完整 KMI**：确认 `androidXX-X.XX`，不要只读取 `uname -r` 开头的主版本。
3. **厂商 ABI**：厂商修改是否仍保持目标 GKI ABI，符号版本是否一致。
4. **内核配置**：LKM、Kprobes/Tracepoint、SuSFS 等所需选项是否实际启用。
5. **发布身份**：Manager 包名、APK 证书与内核预期身份是否来自同一 MakoSU Release。

可先采集基础信息：

```bash
adb shell uname -a
adb shell uname -r
adb shell getprop ro.build.version.release
adb shell getprop ro.product.device
adb shell getprop ro.boot.slot_suffix
```

这些信息只能用于初步判断，不能代替设备维护者的内核说明与实际符号检查。

## 为什么同一 5.10 仍可能不兼容

`android12-5.10` 与 `android13-5.10` 属于不同 KMI 世代。厂商还可能修改导出符号、启用模块签名、关闭模块加载能力，或在相同版本字符串下使用不同配置。强制加载不匹配的 LKM 可能直接失败，也可能在重启后崩溃。

## GKI 1.0 与内核 5.4

::: danger 当前不属于正式支持
Android 11 / 5.4（GKI 1.0）不在 MakoSU 当前正式发布与内置 KMI 范围内。实验源码不能证明一个模块可以通吃所有 5.4 设备。
:::

5.4 设备之间的厂商改动、符号与配置差异更大。若要维护这类设备，需要针对具体设备内核源码、编译配置和 ABI 单独构建、验证和提供恢复方案。

## 自定义内核

自定义内核应由维护者明确说明：

- 使用的 MakoSU/KernelSU 提交与集成模式。
- 目标设备、ROM、Android 世代和内核构建版本。
- 是否集成 SuSFS，以及启用了哪些内核侧能力。
- Manager 身份与推荐版本。
- 可恢复的原始镜像和已验证刷写步骤。

没有这些信息时，不要通过反复尝试 KMI 来碰运气。

## Manager 身份契约

| 项目          | 正式值                                                             |
| ------------- | ------------------------------------------------------------------ |
| 应用包名      | `com.makosu.manager`                                               |
| 证书 DER 大小 | `0x0549`                                                           |
| 证书 SHA-256  | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

改动任一身份字段后，都必须重建并检查全部正式 KMI，再验证 APK v2 证书。只修改应用名、包名或 APK 签名会造成 Manager 无法被内核识别。
