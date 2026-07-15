# MakoSU

<img align="right" src="../MakoSU-mini.png" width="220px" alt="MakoSU logo">

[English](../README.md) | [简体中文](../zh/README.md) | [繁體中文](../zh-TW/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | [Русский](../ru/README.md) | **Türkçe**

MakoSU, [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) tabanlı bir alt projedir. Bu depo Manager'ı, sürümlerde paketlenen KMI modüllerini, SuSFS userspace bileşenlerini ve ilgili derleme betiklerini barındırır.

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-orange.svg?logo=gnu)](../../LICENSE)
[![Manager](https://img.shields.io/badge/Manager-Android%208.0%2B-3DDC84.svg?logo=android)](#uyumluluk)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f81f7.svg)](#paketlenen-kmiler)

> [!WARNING]
> MakoSU önyükleme imajını değiştirir veya çekirdek modülü yükler. Uyumsuz çekirdek, LKM, imza ya da bölüm cihazı açılmaz hâle getirebilir. Kurulumdan önce orijinal imajı yedekleyin ve çalışan bir Fastboot/Recovery kurtarma yöntemi hazırlayın.

## Özellikler

- Çekirdek tabanlı `su`, yetki yönetimi ve App Profile.
- LKM kurulumu, önyükleme imajı yamalama ve otomatik KMI algılama.
- Manager paket adı: `com.makosu.manager`.
- KPM, modül yönetimi, çekirdek flaşlama ve bakım araçları.
- Material ve Miuix arayüzleri ile tema değiştirme.
- SuSFS yol, harita, Kstat, uname, günlük ve otomatik başlatma ayarları.
- Release imza bilgileri eksikse derleme başarısız olur; Debug sertifikasına sessizce dönülmez.
- `arm64-v8a`, `armeabi-v7a` ve `x86_64` userspace bileşenleri.

## SuSFS

- Süreçler arası kilit, eş zamanlı yapılandırma yazımlarının birbirini ezmesini engeller.
- Geçici dosya, `fsync` ve atomik değiştirme yapılandırma bozulması riskini azaltır.
- Kesilmiş veri, yinelenen anahtar, aşırı büyük alan ve sonda kalan baytlar reddedilir.
- Manager tüm yapılandırmayı tek root komutuyla okuyarak arayüz gecikmesini azaltır.
- Otomatik başlatma modülü önce hazırlanır ve eşitlenir; etkinleştirme başarısız olursa geri alınır.
- Depolama beklemesi zaman aşımına sahiptir; sonsuz döngü ve sabit 45 saniye bekleme kaldırılmıştır.
- Yedek geri yükleme veya modül güncellemesi başarısız olduğunda önceki durum geri yüklenmeye çalışılır.
- Shell argümanları güvenli biçimde kaçırılır ve depolama biçimiyle uyumsuz ayraçlar reddedilir.

Kullanılabilir SuSFS özellikleri cihaz çekirdeğindeki entegrasyona bağlıdır.

## Uyumluluk

Mevcut resmi destek GKI 2.0 ve `5.10` ya da daha yeni çekirdeklerdir. Manager için minimum sürüm Android 8.0 / API 26'dır.

Android 11 / 5.4 (GKI 1.0) resmi sürüme veya paketlenen KMI listesine dahil değildir. Deneysel 5.4 kaynakları tüm 5.4 çekirdeklerle evrensel uyumluluk anlamına gelmez.

## Paketlenen KMI'ler

| Android | Çekirdek | KMI |
| --- | --- | --- |
| Android 12 | 5.10 | `android12-5.10` |
| Android 13 | 5.10 | `android13-5.10` |
| Android 13 | 5.15 | `android13-5.15` |
| Android 14 | 5.15 | `android14-5.15` |
| Android 14 | 6.1 | `android14-6.1` |
| Android 15 | 6.6 | `android15-6.6` |
| Android 16 | 6.12 | `android16-6.12` |

Yalnızca ana çekirdek sürümüne bakarak LKM yüklemeyin. Üretici ABI'si, semboller, çekirdek yapılandırması ve KMI işareti de eşleşmelidir.

## Manager kimlik sözleşmesi

| Alan | Değer |
| --- | --- |
| Paket | `com.makosu.manager` |
| Sertifika DER boyutu | `0x0549` |
| Sertifika SHA-256 | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

Kimlik alanlarından biri değişirse tüm KMI'ler yeniden derlenmeli ve APK v2 sertifikası tekrar doğrulanmalıdır.

## Kaynaktan derleme

```bash
bash scripts/build-makosu-kmi.sh
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-makosu-rust.ps1
Set-Location .\manager
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat assembleRelease
```

Release imzası `manager/makosu-signing.properties` veya CI Secret ile yapılandırılır. Anahtarları ve parolaları depoya eklemeyin.

## Görseller, lisans ve sorumluluk reddi

README `docs/MakoSU-mini.png` dosyasını kullanır; özgün görsel `docs/MakoSU.png` olarak saklanır.

*Senren Banka* ile ilişkili karakter, ad ve görsellerin hakları YUZUSOFT ve ilgili hak sahiplerine aittir. MakoSU resmi olmayan bir bakım projesidir; YUZUSOFT ile bağlantılı, onaylı veya sponsorlu değildir. Kaynak kod lisansı bu görselleri kullanma hakkı vermez.

Çekirdek kodu dosya düzeyindeki GPL-2.0-only bildirimlerine, diğer kod kök [`LICENSE`](../../LICENSE) ve dosya bildirimlerine tabidir. Kilit açma, flaşlama, Root, veri kaybı ve cihaz hasarı riski kullanıcıya aittir.

## Teşekkürler

[KernelSU](https://github.com/tiann/KernelSU), [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra), [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU), [MKSU](https://github.com/5ec1cff/KernelSU), [RKSU](https://github.com/rsuntk/KernelsU), [susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu), [KernelPatch](https://github.com/bmax121/KernelPatch) ve [Magisk](https://github.com/topjohnwu/Magisk) geliştiricilerine teşekkür ederiz.
