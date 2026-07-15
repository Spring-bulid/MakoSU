# MakoSU

<img align="right" src="../MakoSU-mini.png" width="220px" alt="MakoSU logo">

[English](../README.md) | [简体中文](../zh/README.md) | [繁體中文](../zh-TW/README.md) | [日本語](../ja/README.md) | [한국어](../ko/README.md) | **Русский** | [Türkçe](../tr/README.md)

MakoSU — downstream-проект [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra). В этом репозитории поддерживаются Manager, KMI-модули для выпусков, userspace-часть SuSFS и связанные скрипты сборки.

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-orange.svg?logo=gnu)](../../LICENSE)
[![Manager](https://img.shields.io/badge/Manager-Android%208.0%2B-3DDC84.svg?logo=android)](#совместимость)
[![KMI](https://img.shields.io/badge/KMI-5.10--6.12-2f81f7.svg)](#встроенные-kmi)

> [!WARNING]
> MakoSU изменяет загрузочный образ или загружает модуль ядра. Несовместимое ядро, LKM, подпись или раздел могут сделать устройство незагружаемым. Сохраните оригинальный образ и подготовьте рабочее восстановление через Fastboot или Recovery.

## Возможности

- Ядерный `su`, управление разрешениями и App Profile.
- Установка LKM, патч загрузочного образа и автоматическое определение KMI.
- Имя пакета Manager: `com.makosu.manager`.
- KPM, управление модулями, прошивка ядра и сервисные инструменты.
- Интерфейсы Material и Miuix с переключением темы.
- Настройка SuSFS: пути, карты, Kstat, uname, журналирование и автозапуск.
- Release-сборка завершается ошибкой без параметров подписи и не переходит на Debug-ключ.
- Userspace-компоненты для `arm64-v8a`, `armeabi-v7a` и `x86_64`.

## SuSFS

- Межпроцессная блокировка предотвращает потерю параллельных изменений конфигурации.
- Временный файл, `fsync` и атомарная замена уменьшают риск повреждения настроек.
- Парсер отклоняет обрезанные данные, повторяющиеся ключи, слишком большие поля и лишние байты.
- Manager получает всю конфигурацию одной root-командой, уменьшая задержки интерфейса.
- Модуль автозапуска сначала полностью создаётся во временном каталоге и откатывается при ошибке.
- Ожидание хранилища ограничено по времени; бесконечные циклы и фиксированная задержка 45 секунд удалены.
- При ошибке восстановления резервной копии или обновления модуля выполняется попытка вернуть прежнее состояние.
- Аргументы Shell экранируются, а небезопасные для формата разделители отклоняются.

Доступные функции SuSFS зависят от интеграции в ядро устройства.

## Совместимость

Официальный диапазон текущего выпуска: GKI 2.0 и ядро `5.10` или новее. Минимальная версия Android для Manager — Android 8.0 / API 26.

Android 11 / 5.4 (GKI 1.0) не входит в официальный выпуск и набор встроенных KMI. Экспериментальный код 5.4 не означает универсальную совместимость со всеми ядрами 5.4.

## Встроенные KMI

| Android | Ядро | KMI |
| --- | --- | --- |
| Android 12 | 5.10 | `android12-5.10` |
| Android 13 | 5.10 | `android13-5.10` |
| Android 13 | 5.15 | `android13-5.15` |
| Android 14 | 5.15 | `android14-5.15` |
| Android 14 | 6.1 | `android14-6.1` |
| Android 15 | 6.6 | `android15-6.6` |
| Android 16 | 6.12 | `android16-6.12` |

Не загружайте LKM только по основной версии ядра. ABI производителя, символы, конфигурация ядра и метка KMI также должны совпадать.

## Контракт идентичности Manager

| Поле | Значение |
| --- | --- |
| Пакет | `com.makosu.manager` |
| Размер DER-сертификата | `0x0549` |
| SHA-256 сертификата | `7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943` |

При изменении любого поля необходимо пересобрать все KMI и повторно проверить v2-сертификат APK.

## Сборка

```bash
bash scripts/build-makosu-kmi.sh
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-makosu-rust.ps1
Set-Location .\manager
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat assembleRelease
```

Параметры Release-подписи задаются в `manager/makosu-signing.properties` или CI Secret. Никогда не добавляйте ключи и пароли в репозиторий.

## Изображения, лицензия и отказ от ответственности

README использует `docs/MakoSU-mini.png`; исходное изображение сохранено как `docs/MakoSU.png`.

Права на персонажей, названия и изображения, связанные с *Senren Banka*, принадлежат YUZUSOFT и соответствующим правообладателям. MakoSU является неофициальным проектом и не связан, не одобрен и не спонсируется YUZUSOFT. Лицензия исходного кода не предоставляет право использовать эти изображения.

Код ядра следует файловым объявлениям GPL-2.0-only, остальной код — корневому [`LICENSE`](../../LICENSE) и файловым лицензиям. Риски разблокировки, прошивки, Root, потери данных и повреждения устройства несёт пользователь.

## Благодарности

[KernelSU](https://github.com/tiann/KernelSU), [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra), [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU), [MKSU](https://github.com/5ec1cff/KernelSU), [RKSU](https://github.com/rsuntk/KernelsU), [susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu), [KernelPatch](https://github.com/bmax121/KernelPatch) и [Magisk](https://github.com/topjohnwu/Magisk).
