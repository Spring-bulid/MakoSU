---
title: MakoSU 文档
description: MakoSU 安装、KMI 兼容、SuSFS 与故障恢复指南。
outline: false
---

# MakoSU 文档

这里整理 MakoSU 正式发布版本的安装、兼容性判断、SuSFS 配置和故障恢复方法。文档只描述当前可验证的发布范围，不把实验代码视为正式支持。

::: danger 刷写有风险
MakoSU 会修改启动镜像或加载内核模块。开始前请备份原始镜像，确认 Fastboot 或 Recovery 可用，并记录当前活动槽位。错误的内核、LKM、签名身份或目标分区都可能导致设备无法启动。
:::

## 从这里开始

- [安装与更新](/guide/installation)：下载校验、首次安装、LKM 选择和更新前检查。
- [KMI 兼容性](/guide/compatibility)：确认设备是否属于 7 组正式 KMI，理解为什么不能只看内核主版本。
- [SuSFS 使用](/guide/susfs)：功能前提、推荐配置顺序、备份与恢复。
- [故障排查与救砖](/guide/troubleshooting)：管理器未识别、LKM 加载失败、启动异常和日志采集。

## 当前正式范围

| 项目             | 正式发布范围               |
| ---------------- | -------------------------- |
| Manager 最低系统 | Android 8.0 / API 26       |
| 内核模式         | GKI 2.0                    |
| 内核版本         | 5.10、5.15、6.1、6.6、6.12 |
| KMI 数量         | 7 组                       |
| Manager 包名     | `com.makosu.manager`       |

Android 11 / 5.4（GKI 1.0）不在当前正式发布与内置 KMI 范围内。仓库中出现实验性 5.4 代码，不代表所有 5.4 内核可以通用。

## 获取发布版本

从 [GitHub Releases](https://github.com/Spring-bulid/MakoSU/releases) 获取 APK 与对应发布包。不要混用其他包名、其他证书签名的 Manager，或来源不明的 `kernelsu.ko`。

项目按现状提供，不保证适配所有设备与厂商内核。解锁、Root、数据丢失、保修失效和设备损坏风险由使用者承担。
