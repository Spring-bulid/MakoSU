# MakoSU

<img align="right" src="MakoSU-mini.png" width="220px" alt="MakoSU logo">

**English** | [简体中文](zh/README.md) | [繁體中文](zh-TW/README.md) | [日本語](ja/README.md) | [한국어](ko/README.md) | [Русский](ru/README.md) | [Türkçe](tr/README.md)

MakoSU is a downstream of [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra). This repository maintains the Manager, release KMI modules, SuSFS userspace support, and the related build scripts.

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-orange.svg?logo=gnu)](../LICENSE)
[![Manager](https://img.shields.io/badge/Manager-Android%208.0%2B-3DDC84.svg?logo=android)](#compatibility)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f81f7.svg)](#bundled-kmis)
[![SuSFS](https://img.shields.io/badge/SuSFS-transactional-4c8bf5.svg)](#susfs)

> [!WARNING]
> MakoSU patches boot images or loads kernel modules. A mismatched kernel, LKM, signing identity, or target partition can make a device unbootable. Back up the original boot image and keep a tested recovery path before installation.

## Features

- Kernel-based `su`, authorization management, and App Profile.
- LKM installation, boot-image patching, and automatic KMI matching.
- Manager package name: `com.makosu.manager`.
- KPM, module management, kernel flashing, and maintenance tools.
- Material and Miuix interfaces with theme switching.
- SuSFS path, map, Kstat, uname, logging, and auto-start management.
- Release builds fail when signing properties are missing; no silent Debug-key fallback.
- Manager userspace components for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.

## SuSFS

The current SuSFS userspace implementation includes:

- Cross-process locking prevents concurrent config writes from overwriting each other.
- Temporary-file writes, `fsync`, and atomic replacement reduce corruption after a crash or power loss.
- The binary parser rejects truncation, duplicate keys, oversized fields, and trailing bytes.
- The Manager reads the complete configuration in one root command instead of launching many sequential processes.
- The auto-start module is staged, synchronized, activated, and rolled back on failure.
- Storage waits are bounded; generated scripts no longer wait forever or sleep for a fixed 45 seconds.
- Backup restore replaces configuration in one operation and attempts to restore the previous module state on failure.
- User-controlled shell arguments are quoted consistently, and unsupported separators are rejected before persistence.

Available SuSFS features still depend on the device kernel. The Manager cannot add kernel-side SuSFS support to a kernel that does not include it.

## Compatibility

The current supported release range is GKI 2.0 with kernel `5.10` or newer.

- Minimum Manager Android version: Android 8.0 / API 26.
- Automatic matching requires a valid Android KMI marker in the kernel version string.
- Custom kernels must integrate MakoSU/KernelSU correctly and match the Manager identity contract.
- Android 11 / kernel 5.4 (GKI 1.0) is not part of the current release or bundled KMI set.
- Experimental 5.4 source and scripts do not imply universal or production support.

## Bundled KMIs

The release bundle contains exactly these seven modules:

| Android generation | Kernel | KMI |
| --- | --- | --- |
| Android 12 | 5.10 | `android12-5.10` |
| Android 13 | 5.10 | `android13-5.10` |
| Android 13 | 5.15 | `android13-5.15` |
| Android 14 | 5.15 | `android14-5.15` |
| Android 14 | 6.1 | `android14-6.1` |
| Android 15 | 6.6 | `android15-6.6` |
| Android 16 | 6.12 | `android16-6.12` |

Do not force-load an LKM based only on the major kernel version. Vendor ABI, symbols, configuration, and KMI markers must also match.

## Installation

1. Obtain the signed MakoSU Release APK and its matching release bundle.
2. Verify the published SHA-256 values and the bundle's `SHA256SUMS.txt`.
3. Install the Manager and allow it to detect the current KMI.
4. Select an LKM manually only when the exact compatibility is known.
5. Back up the original image before patching or direct installation.
6. Keep a working Fastboot or Recovery restoration method.

Do not mix MakoSU with a Manager signed by another certificate or with an unknown `kernelsu.ko`.

## Manager identity contract

| Field | Release value |
| --- | --- |
| Application package | `com.makosu.manager` |
| Certificate DER size | `0x0549` |
| Certificate SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

Changing any identity field requires rebuilding and inspecting every supported KMI and verifying the APK v2 certificate again.

## Building from source

Requirements: Git, Rust stable with Android targets, JDK 17, Android SDK, Build Tools 37, NDK `29.0.14206865`, and Docker for KMI builds.

Build the seven KMIs from Bash:

```bash
bash scripts/build-makosu-kmi.sh
```

Build the three Rust ABIs and the Debug Manager from Windows PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-makosu-rust.ps1
Set-Location .\manager
.\gradlew.bat testDebugUnitTest assembleDebug
```

Release signing is configured in `manager/makosu-signing.properties` or CI secrets:

```properties
KEYSTORE_FILE=path/to/release.jks
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=your_alias
KEY_PASSWORD=your_key_password
```

Never commit keys or passwords.

```powershell
Set-Location .\manager
.\gradlew.bat assembleRelease
apksigner verify --verbose --print-certs .\app\build\outputs\apk\release\MakoSU_*-release.apk
```

## Quality checks

```bash
cargo fmt --manifest-path userspace/ksud/Cargo.toml --check
cargo test --manifest-path userspace/ksud/Cargo.toml --lib
```

Run Android-targeted Clippy after Rust changes and ShellCheck after shell changes. Manager changes require unit tests and `assembleDebug`. Before publishing a release, build the signed APK, verify its v2 certificate, and inspect the packaged KMI set.

See [`MAINTENANCE.md`](../MAINTENANCE.md) and [`CONTRIBUTING.md`](../CONTRIBUTING.md) for maintenance rules.

## Visual assets and trademarks

The README uses `docs/MakoSU-mini.png`; the original artwork is preserved as `docs/MakoSU.png`.

Characters, titles, and visual material related to *Senren Banka* are owned by YUZUSOFT and the respective rightsholders. MakoSU is an unofficial maintenance project and is not affiliated with, authorized by, or sponsored by YUZUSOFT.

The source-code license does not grant permission to use, modify, or redistribute those character or brand assets. Obtain the required authorization before using them.

## License

- Kernel code follows the file-level declarations and GPL-2.0-only requirements.
- Other source is governed by the root [`LICENSE`](../LICENSE) and file-level SPDX/license notices.
- Third-party code, fonts, icons, character art, and trademarks retain their own terms.

## Credits

- [KernelSU](https://github.com/tiann/KernelSU): kernel-root foundation.
- [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra): direct upstream.
- [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU): engineering and maintenance reference.
- [MKSU](https://github.com/5ec1cff/KernelSU): Magic Mount and related work.
- [RKSU](https://github.com/rsuntk/KernelsU): non-GKI work.
- [susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu): SuSFS kernel and userspace work.
- [KernelPatch](https://github.com/bmax121/KernelPatch): KPM/APatch foundation.
- [Magisk](https://github.com/topjohnwu/Magisk): Android root and module ecosystem.

## Disclaimer

This project is provided as-is without a guarantee of compatibility with every device, vendor kernel, or Android release. Unlocking, flashing, rooting, data loss, warranty impact, and device damage remain the user's responsibility.
