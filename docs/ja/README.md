# MakoSU

<img align="right" src="../MakoSU-mini.png" width="220px" alt="MakoSU logo">

[English](../README.md) | [简体中文](../zh/README.md) | [繁體中文](../zh-TW/README.md) | **日本語** | [한국어](../ko/README.md) | [Русский](../ru/README.md) | [Türkçe](../tr/README.md)

MakoSU は [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) の downstream プロジェクトです。このリポジトリでは Manager、リリース用 KMI モジュール、SuSFS の userspace 機能、関連ビルドスクリプトを保守しています。

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-orange.svg?logo=gnu)](../../LICENSE)
[![Manager](https://img.shields.io/badge/Manager-Android%208.0%2B-3DDC84.svg?logo=android)](#互換性)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f81f7.svg)](#同梱-kmi)

> [!WARNING]
> MakoSU はブートイメージを変更し、またはカーネルモジュールを読み込みます。互換性のないカーネル、LKM、署名、パーティションを使用すると起動不能になる可能性があります。必ず元のイメージと復旧手段を準備してください。

## 主な機能

- カーネルベースの `su`、権限管理、App Profile。
- LKM インストール、ブートイメージのパッチ、KMI 自動判定。
- Manager パッケージ名：`com.makosu.manager`。
- KPM、モジュール管理、カーネルフラッシュ機能。
- Material / Miuix UI とテーマ切り替え。
- SuSFS のパス、マップ、Kstat、uname、ログ、自動起動設定。
- Release 署名がない場合はビルドを失敗させ、Debug 署名へ自動フォールバックしません。
- `arm64-v8a`、`armeabi-v7a`、`x86_64` の userspace コンポーネント。

## SuSFS

MakoSU の SuSFS userspace 実装には、次の保護が含まれます。

- プロセス間ロックによる設定更新の競合防止。
- 一時ファイル、`fsync`、アトミック置換による設定破損対策。
- 途中で切れたデータ、重複キー、上限超過、余分なデータの拒否。
- Manager は 1 回の root コマンドで設定全体を取得し、画面の待ち時間を削減。
- 自動起動モジュールはステージング後に切り替え、失敗時は旧状態へロールバック。
- ストレージ待機には上限があり、無限ループや固定 45 秒待機を行いません。
- バックアップ復元とモジュール更新に失敗した場合、旧設定の復元を試みます。
- Shell 引数を統一的にエスケープし、保存形式で扱えない区切り文字を拒否します。

利用可能な SuSFS 機能はカーネル側の設定に依存します。Manager だけで未統合のカーネルに SuSFS を追加することはできません。

## 互換性

現在の正式サポートは GKI 2.0、カーネル `5.10` 以上です。

- Manager の最低 Android バージョン：Android 8.0 / API 26。
- 自動判定にはカーネル文字列内の有効な Android KMI マーカーが必要です。
- Android 11 / 5.4（GKI 1.0）は現在の正式リリースに含まれません。
- 5.4 向けの実験コードは、すべての 5.4 カーネルで動作することを意味しません。

## 同梱 KMI

| Android | カーネル | KMI |
| --- | --- | --- |
| Android 12 | 5.10 | `android12-5.10` |
| Android 13 | 5.10 | `android13-5.10` |
| Android 13 | 5.15 | `android13-5.15` |
| Android 14 | 5.15 | `android14-5.15` |
| Android 14 | 6.1 | `android14-6.1` |
| Android 15 | 6.6 | `android15-6.6` |
| Android 16 | 6.12 | `android16-6.12` |

メジャーバージョンだけを見て LKM を強制読み込みしないでください。ベンダー ABI、シンボル、設定、KMI が一致する必要があります。

## インストール

1. 署名済み Release APK と対応するリリース ZIP を取得します。
2. SHA-256 と ZIP 内の `SHA256SUMS.txt` を確認します。
3. 元のブートイメージをバックアップします。
4. Manager の自動 KMI 判定を使用し、完全に一致する場合のみ手動選択します。
5. Fastboot または Recovery から復旧できる状態を維持します。

別の証明書で署名された Manager や、出所不明の `kernelsu.ko` と混在させないでください。

## Manager 署名契約

| 項目 | 値 |
| --- | --- |
| パッケージ | `com.makosu.manager` |
| 証明書 DER サイズ | `0x0549` |
| 証明書 SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

この契約を変更した場合は、すべての KMI を再ビルドし、APK v2 証明書を再確認する必要があります。

## ソースからのビルド

必要環境：Git、Rust stable、JDK 17、Android SDK、Build Tools 37、NDK `29.0.14206865`、KMI 用 Docker。

```bash
bash scripts/build-makosu-kmi.sh
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-makosu-rust.ps1
Set-Location .\manager
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat assembleRelease
```

Release 署名は `manager/makosu-signing.properties` または CI Secret から設定します。鍵とパスワードをコミットしないでください。

## 商標とライセンス

README では `docs/MakoSU-mini.png` を使用し、元画像は `docs/MakoSU.png` に保存しています。

『千恋＊万花』に関するキャラクター、名称、画像の権利は YUZUSOFT および各権利者に帰属します。MakoSU は非公式プロジェクトであり、YUZUSOFT との提携、認可、協賛関係はありません。ソースコードのライセンスは、これらの素材の利用権を付与しません。

カーネルコードは各ファイルの GPL-2.0-only 表示に従い、その他のコードはルートの [`LICENSE`](../../LICENSE) と各ファイルのライセンス表示に従います。

## 謝辞

[KernelSU](https://github.com/tiann/KernelSU)、[SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra)、[ReSukiSU](https://github.com/ReSukiSU/ReSukiSU)、[MKSU](https://github.com/5ec1cff/KernelSU)、[RKSU](https://github.com/rsuntk/KernelsU)、[susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu)、[KernelPatch](https://github.com/bmax121/KernelPatch)、[Magisk](https://github.com/topjohnwu/Magisk) の開発者に感謝します。

## 免責事項

本プロジェクトは現状のまま提供され、すべての端末やベンダーカーネルでの動作を保証しません。アンロック、フラッシュ、Root、データ損失、保証、端末故障のリスクは利用者が負担します。
