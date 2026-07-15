# MakoSU Maintenance Rules

## Scope

- Keep downstream changes narrow, reviewable, and independent from unrelated formatting churn.
- Do not copy project files or workflows from another downstream without adapting names, paths, tool versions, and secrets.
- Preserve the upstream source package namespace unless a complete source-package migration is deliberately planned.

## Identity And Releases

- The manager package, kernel expected certificate size, kernel expected certificate hash, and release APK certificate are one contract. Change them together and verify the APK v2 certificate before publishing.
- A release build must fail when signing properties are missing. Never publish an APK that silently falls back to a debug certificate.
- Keep signing material in `manager/makosu-signing.properties` or CI secrets. Never commit keys or passwords.
- Rebuild and inspect all supported KMI modules whenever the manager certificate or package identity changes.

## Source Quality

- Keep shell, Rust, Kotlin, XML, YAML, and Gradle Kotlin files LF-only. Batch files use CRLF.
- Run `cargo fmt --check` for `userspace/ksud` and `userspace/ksuinit` after Rust changes.
- Run Android-targeted Clippy for Rust changes and `shellcheck` for shell-script changes.
- Run `manager/gradlew assembleDebug` for manager UI changes. Run the signed release build and APK signature verification before release publication.
- Add focused regression tests whenever a bug fix changes persisted state, manager identity, module installation, or release packaging.

## CI

- CI must build debug artifacts without requiring release secrets.
- Release workflows may use signing secrets, but pull-request workflows must never expose them.
- Keep CodeQL, line-ending checks, Rust formatting, Android-targeted Clippy, and ShellCheck enabled for their relevant paths.
