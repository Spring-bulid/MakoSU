# MakoSU build tasks
alias bk := build_ksud
alias bm := build_manager

build_ksud:
    cargo build --target aarch64-linux-android --release --manifest-path ./userspace/ksud/Cargo.toml

build_manager: build_ksud
    cp userspace/ksud/target/aarch64-linux-android/release/ksud manager/app/src/main/jniLibs/arm64-v8a/libksud.so
    cd manager && ./gradlew assembleDebug

clippy:
    cargo fmt --manifest-path ./userspace/ksud/Cargo.toml
    cargo clippy --target aarch64-linux-android --release --manifest-path ./userspace/ksud/Cargo.toml
    cargo clippy --target x86_64-linux-android --release --manifest-path ./userspace/ksud/Cargo.toml