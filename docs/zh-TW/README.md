# MakoSU

<img align="right" src="../MakoSU-mini.png" width="220px" alt="MakoSU logo">

[English](../README.md) | [简体中文](../zh/README.md) | **繁體中文** | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Русский](../ru/README.md) | [Türkçe](../tr/README.md)

MakoSU 是 [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) 的下游專案。本儲存庫維護 Manager、隨版本發佈的 KMI 模組、SuSFS userspace 功能及相關建置腳本。

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-orange.svg?logo=gnu)](../../LICENSE)
[![Manager](https://img.shields.io/badge/Manager-Android%208.0%2B-3DDC84.svg?logo=android)](#相容範圍)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f81f7.svg)](#內建-kmi)

> [!WARNING]
> MakoSU 會修改啟動映像或載入核心模組。錯誤的核心、LKM、簽章身分或分割區可能造成裝置無法開機。操作前請備份原始映像並準備可用的救援方式。

## 主要功能

- 核心級 `su`、授權管理與 App Profile。
- LKM 安裝、啟動映像修補與 KMI 自動辨識。
- Manager 套件名稱：`com.makosu.manager`。
- KPM、模組管理、核心刷寫與維護工具。
- Material、Miuix 介面與主題切換。
- SuSFS 路徑、映射、Kstat、uname、日誌與自動啟動管理。
- Release 簽章設定缺失時直接讓建置失敗，不會退回 Debug 憑證。
- `arm64-v8a`、`armeabi-v7a`、`x86_64` 使用者空間元件。

## SuSFS

- 跨程序鎖避免同時寫入設定時互相覆蓋。
- 暫存檔、`fsync` 與原子替換降低設定檔損毀風險。
- 拒絕截斷資料、重複鍵、過大欄位與尾隨資料。
- Manager 只需一次 root 命令即可取得完整設定，降低畫面卡頓。
- 自動啟動模組採暫存、同步、切換與失敗回復流程。
- 儲存空間等待具有逾時上限，不再無限等待或固定休眠 45 秒。
- 備份復原或模組更新失敗時，會嘗試恢復舊設定與舊模組狀態。
- Shell 參數統一轉義，無法安全儲存的分隔符會被拒絕。

實際可用的 SuSFS 功能仍取決於裝置核心是否已整合相應功能。

## 相容範圍

目前正式支援 GKI 2.0、核心 `5.10` 以上版本。Manager 最低支援 Android 8.0 / API 26。

Android 11 / 5.4（GKI 1.0）目前不在正式發佈與內建 KMI 範圍內。實驗性 5.4 程式碼不代表所有 5.4 核心皆可通用。

## 內建 KMI

| Android 世代 | 核心 | KMI |
| --- | --- | --- |
| Android 12 | 5.10 | `android12-5.10` |
| Android 13 | 5.10 | `android13-5.10` |
| Android 13 | 5.15 | `android13-5.15` |
| Android 14 | 5.15 | `android14-5.15` |
| Android 14 | 6.1 | `android14-6.1` |
| Android 15 | 6.6 | `android15-6.6` |
| Android 16 | 6.12 | `android16-6.12` |

請勿只依核心主版本強制載入 LKM；廠商 ABI、符號、核心設定與 KMI 標記都必須相符。

## Manager 身分契約

| 項目 | 值 |
| --- | --- |
| 套件名稱 | `com.makosu.manager` |
| 憑證 DER 大小 | `0x0549` |
| 憑證 SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

變更任何一項都必須重新建置所有支援的 KMI，並再次驗證 APK v2 憑證。

## 從原始碼建置

```bash
bash scripts/build-makosu-kmi.sh
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-makosu-rust.ps1
Set-Location .\manager
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat assembleRelease
```

Release 簽章資料放在 `manager/makosu-signing.properties` 或 CI Secret。請勿提交金鑰與密碼。

## 視覺素材、授權與免責聲明

README 使用 `docs/MakoSU-mini.png`，高清原圖為 `docs/MakoSU.png`。

與《千戀＊萬花》相關的角色、名稱與視覺素材，其權利歸 YUZUSOFT 及相應權利人所有。MakoSU 為非官方維護專案，與 YUZUSOFT 無隸屬、授權或合作關係；程式碼授權不包含上述素材的使用權。

核心程式碼依各檔案的 GPL-2.0-only 聲明，其餘程式碼依根目錄 [`LICENSE`](../../LICENSE) 與檔案內聲明。解鎖、刷寫、Root、資料遺失與裝置損壞風險由使用者自行承擔。

## 鳴謝

[KernelSU](https://github.com/tiann/KernelSU)、[SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra)、[ReSukiSU](https://github.com/ReSukiSU/ReSukiSU)、[MKSU](https://github.com/5ec1cff/KernelSU)、[RKSU](https://github.com/rsuntk/KernelsU)、[susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu)、[KernelPatch](https://github.com/bmax121/KernelPatch) 與 [Magisk](https://github.com/topjohnwu/Magisk)。
