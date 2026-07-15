# Contributing to MakoSU

## Before Opening A Pull Request

- Start from the latest `main` and keep each pull request focused on one behavior or maintenance concern.
- Explain the user-visible impact, compatibility implications, and rollback path.
- Do not include generated outputs, private signing material, local caches, or unrelated formatting changes.

## Required Checks

- Rust: run `cargo fmt --check` and Android-targeted Clippy for changed Rust crates.
- Shell: run ShellCheck for changed `.sh` files and preserve LF line endings.
- Manager: run `./gradlew :app:assembleDebug` for Kotlin, resource, or Gradle changes.
- Release identity: when package or signing data changes, rebuild all KMI modules and verify the APK v2 certificate hash.

## Bug Reports

Use the issue template, include exact reproduction steps, device/kernel details, and an exported MakoSU log. Reports without enough information to reproduce may be closed.

## Security

Do not report security-sensitive issues in a public issue. Follow `SECURITY.md` instead.
