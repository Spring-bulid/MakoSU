# MakoSU

<img align="right" src="../MakoSU-mini.png" width="220px" alt="MakoSU logo">

[English](../README.md) | [简体中文](../zh/README.md) | [繁體中文](../zh-TW/README.md) | [日本語](../ja/README.md) | **한국어** | [Русский](../ru/README.md) | [Türkçe](../tr/README.md)

MakoSU는 [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra)의 다운스트림 프로젝트입니다. 이 저장소는 Manager, 릴리스 KMI 모듈, SuSFS 사용자 공간 기능과 관련 빌드 스크립트를 관리합니다.

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-orange.svg?logo=gnu)](../../LICENSE)
[![Manager](https://img.shields.io/badge/Manager-Android%208.0%2B-3DDC84.svg?logo=android)](#호환성)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f81f7.svg)](#포함된-kmi)

> [!WARNING]
> MakoSU는 부트 이미지를 수정하거나 커널 모듈을 로드합니다. 호환되지 않는 커널, LKM, 서명 또는 파티션을 사용하면 부팅할 수 없게 될 수 있습니다. 설치 전에 원본 이미지를 백업하고 Fastboot/Recovery 복구 방법을 준비하십시오.

## 주요 기능

- 커널 기반 `su`, 권한 관리와 App Profile.
- LKM 설치, 부트 이미지 패치와 KMI 자동 감지.
- Manager 패키지 이름: `com.makosu.manager`.
- KPM, 모듈 관리, 커널 플래시와 유지보수 도구.
- Material/Miuix UI와 테마 전환.
- SuSFS 경로, 맵, Kstat, uname, 로그와 자동 시작 설정.
- Release 서명 정보가 없으면 빌드가 실패하며 Debug 인증서로 자동 대체하지 않습니다.
- `arm64-v8a`, `armeabi-v7a`, `x86_64` 사용자 공간 구성 요소.

## SuSFS

- 프로세스 간 잠금으로 동시 설정 저장 시 덮어쓰기를 방지합니다.
- 임시 파일, `fsync`, 원자적 교체로 설정 파일 손상 가능성을 줄입니다.
- 잘린 데이터, 중복 키, 과도한 필드와 후행 데이터를 거부합니다.
- Manager는 한 번의 root 명령으로 전체 설정을 읽어 UI 지연을 줄입니다.
- 자동 시작 모듈은 스테이징, 동기화, 전환 후 실패 시 롤백됩니다.
- 저장소 대기에는 제한 시간이 있으며 무한 대기나 고정 45초 지연을 사용하지 않습니다.
- 백업 복원 또는 모듈 업데이트 실패 시 이전 설정과 모듈 상태 복원을 시도합니다.
- 셸 인수를 일관되게 이스케이프하고 저장 형식에서 안전하지 않은 구분자를 거부합니다.

실제 SuSFS 기능은 장치 커널에 통합된 옵션에 따라 달라집니다.

## 호환성

현재 정식 지원 범위는 GKI 2.0과 커널 `5.10` 이상입니다. Manager 최소 버전은 Android 8.0 / API 26입니다.

Android 11 / 5.4(GKI 1.0)는 현재 정식 릴리스와 포함 KMI 범위에 속하지 않습니다. 실험적인 5.4 소스가 모든 5.4 커널과의 호환성을 의미하지는 않습니다.

## 포함된 KMI

| Android | 커널 | KMI |
| --- | --- | --- |
| Android 12 | 5.10 | `android12-5.10` |
| Android 13 | 5.10 | `android13-5.10` |
| Android 13 | 5.15 | `android13-5.15` |
| Android 14 | 5.15 | `android14-5.15` |
| Android 14 | 6.1 | `android14-6.1` |
| Android 15 | 6.6 | `android15-6.6` |
| Android 16 | 6.12 | `android16-6.12` |

커널 주 버전만 보고 LKM을 강제로 로드하지 마십시오. 공급업체 ABI, 심볼, 커널 설정과 KMI 표시가 모두 일치해야 합니다.

## Manager 식별 계약

| 항목 | 값 |
| --- | --- |
| 패키지 | `com.makosu.manager` |
| 인증서 DER 크기 | `0x0549` |
| 인증서 SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

식별 정보를 변경하면 모든 지원 KMI를 다시 빌드하고 APK v2 인증서를 다시 확인해야 합니다.

## 소스 빌드

```bash
bash scripts/build-makosu-kmi.sh
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-makosu-rust.ps1
Set-Location .\manager
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat assembleRelease
```

Release 서명은 `manager/makosu-signing.properties` 또는 CI Secret으로 설정합니다. 키와 비밀번호를 커밋하지 마십시오.

## 시각 자료, 라이선스와 면책

README는 `docs/MakoSU-mini.png`를 사용하며 원본 이미지는 `docs/MakoSU.png`에 보관됩니다.

*Senren Banka*와 관련된 캐릭터, 이름과 시각 자료의 권리는 YUZUSOFT 및 각 권리자에게 있습니다. MakoSU는 비공식 유지보수 프로젝트이며 YUZUSOFT와 제휴, 승인 또는 후원 관계가 없습니다. 소스 코드 라이선스는 해당 아트워크의 사용 권한을 부여하지 않습니다.

커널 코드는 파일별 GPL-2.0-only 고지를 따르며, 나머지 코드는 루트 [`LICENSE`](../../LICENSE)와 파일별 고지를 따릅니다. 잠금 해제, 플래시, Root, 데이터 손실과 장치 손상 위험은 사용자 책임입니다.

## 감사

[KernelSU](https://github.com/tiann/KernelSU), [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra), [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU), [MKSU](https://github.com/5ec1cff/KernelSU), [RKSU](https://github.com/rsuntk/KernelsU), [susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu), [KernelPatch](https://github.com/bmax121/KernelPatch), [Magisk](https://github.com/topjohnwu/Magisk)의 개발자들에게 감사드립니다.
