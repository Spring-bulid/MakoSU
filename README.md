# MakoSU

<img align="right" src="docs/MakoSU-mini.png" width="220px" alt="MakoSU logo">

[English](docs/README.md) | **简体中文** | [繁體中文](docs/zh-TW/README.md) | [日本語](docs/ja/README.md) | [한국어](docs/ko/README.md) | [Русский](docs/ru/README.md) | [Türkçe](docs/tr/README.md)

面向 GKI Android 设备的内核 Root 管理器，关注 KMI 匹配、SuSFS 配置可靠性和可恢复维护。

[项目官网](https://spring-bulid.github.io/MakoSU/) | [在线文档](https://spring-bulid.github.io/MakoSU/guide/) | [中文修复说明](docs/zh/FIXES.md) | [GitHub Releases](https://github.com/Spring-bulid/MakoSU/releases) | [问题反馈](https://github.com/Spring-bulid/MakoSU/issues)

[![最新发行](https://img.shields.io/github/v/release/Spring-bulid/MakoSU?label=Release&logo=github)](https://github.com/Spring-bulid/MakoSU/releases/latest)
[![网站部署](https://github.com/Spring-bulid/MakoSU/actions/workflows/deploy-website.yml/badge.svg)](https://github.com/Spring-bulid/MakoSU/actions/workflows/deploy-website.yml)
[![协议](https://img.shields.io/badge/License-Multiple-orange.svg?logo=gnu)](LICENSE)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f7259.svg)](https://github.com/Spring-bulid/MakoSU/releases)
[![GitHub Stars](https://img.shields.io/github/stars/Spring-bulid/MakoSU?style=flat)](https://github.com/Spring-bulid/MakoSU)

MakoSU 是 [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) 的下游维护项目，主要维护 Manager、正式 KMI 模块、SuSFS 用户空间功能和相关构建脚本。项目参考 [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU) 的维护思路，优先处理身份契约、失败回滚和可复现构建。

> [!WARNING]
> MakoSU 会修改启动镜像或加载内核模块。安装前必须备份原始镜像，并确认 Fastboot 或 Recovery 可用。错误的内核、LKM、签名身份、槽位或目标分区可能导致设备无法启动。

## 为什么是 MakoSU

- **KMI 感知匹配**：读取完整 Android KMI，避免只看 `5.10` 或 `5.15` 就强行加载模块。
- **SuSFS 可靠配置**：跨进程锁、原子保存、严格解析、单次 Root 读取和失败回滚，减少卡顿与配置损坏。
- **完整维护工具**：覆盖权限管理、LKM、KPM、模块、启动镜像和内核刷写流程。
- **身份契约一致**：Manager 包名、APK Release 证书和内核验证信息作为同一发布契约维护。
- **面向维护者**：保留构建脚本、兼容矩阵、问题模板和恢复文档，方便设备适配与问题定位。

## 当前正式兼容范围

正式发布面向 GKI 2.0、内核 `5.10` 及以上版本，Manager 最低支持 Android 8.0（API 26）。当前 Release 包含以下 7 组 KMI：

| Android | 内核        | KMI                                 |
| ------- | ----------- | ----------------------------------- |
| 12      | 5.10        | `android12-5.10`                    |
| 13      | 5.10 / 5.15 | `android13-5.10` / `android13-5.15` |
| 14      | 5.15 / 6.1  | `android14-5.15` / `android14-6.1`  |
| 15      | 6.6         | `android15-6.6`                     |
| 16      | 6.12        | `android16-6.12`                    |

Android 11 / 5.4（GKI 1.0）不在当前正式发布和内置 KMI 范围内。实验性 5.4 源码不代表所有 5.4 设备通用。

## 快速开始

1. 从 [Releases](https://github.com/Spring-bulid/MakoSU/releases) 下载同一版本的 APK 与 KMI 包。
2. 按照[安装文档](https://spring-bulid.github.io/MakoSU/guide/installation)校验 SHA-256，并备份原始启动镜像。
3. 让 Manager 自动识别 KMI；只有在完整兼容信息一致时才手动选择。
4. 刷写后保留原始镜像和恢复路径，确认重启后 Root、模块和 SuSFS 状态正常。

不要混用其他包名或其他证书签名的 Manager，也不要使用来源不明的 `kernelsu.ko`。

## 参与维护

请先阅读[贡献指南](CONTRIBUTING.md)和[维护规则](MAINTENANCE.md)。提交问题时附上设备型号、Android 版本、完整内核字符串、KMI、MakoSU 版本和可复现步骤；安全漏洞请按照 [SECURITY.md](SECURITY.md) 处理，不要公开发布利用细节。

## 链接

- [MakoSU 官网](https://spring-bulid.github.io/MakoSU/)
- [中文文档](https://spring-bulid.github.io/MakoSU/guide/)
- [English documentation](https://spring-bulid.github.io/MakoSU/en/guide/)
- [English README](docs/README.md)
- [GitHub Releases](https://github.com/Spring-bulid/MakoSU/releases)
- [Issues](https://github.com/Spring-bulid/MakoSU/issues)

## 贡献者

- [irislys](https://github.com/irislys) — MakoSU 在线模块目录与仓库源支持。

## 视觉素材与商标

项目中与《千恋＊万花》相关的角色形象、作品名称和视觉素材，其著作权、商标权及其他知识产权归 YUZUSOFT（柚子社）及相应权利人所有。MakoSU 是非官方维护项目，与 YUZUSOFT 不存在隶属、授权或合作关系。代码许可证不会自动授予相关角色美术和品牌素材的使用权。

## 许可证

内核代码遵循各文件声明及 GPL-2.0-only 要求；其他代码以根目录 [LICENSE](LICENSE) 和文件级许可证声明为准。第三方代码、字体、图标、角色美术和商标仍受各自条款约束。
