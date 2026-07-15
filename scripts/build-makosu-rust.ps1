param(
    [string]$NdkRoot = "$PSScriptRoot\..\.android-sdk\ndk\29.0.14206865",
    [string]$LkmDirectory = "$PSScriptRoot\..\out\lkm",
    [string]$ManagerPackage = "com.makosu.manager",
    [string[]]$ExpectedKmis = @(
        "android12-5.10",
        "android13-5.10",
        "android13-5.15",
        "android14-5.15",
        "android14-6.1",
        "android15-6.6",
        "android16-6.12"
    )
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path "$PSScriptRoot\..").Path
$llvmRoot = Join-Path $NdkRoot "toolchains\llvm\prebuilt\windows-x86_64"
$llvmBin = Join-Path $llvmRoot "bin"
$ksuinit = Join-Path $repoRoot "target\aarch64-unknown-linux-musl\release\ksuinit"
$assetDirectory = Join-Path $repoRoot "userspace\ksud\bin\aarch64"
$outputDirectory = Join-Path $repoRoot "out\rust"
$jniLibDirectory = Join-Path $repoRoot "manager\app\src\main\jniLibs"

function Set-TargetEnvironment {
    param(
        [string]$Target,
        [string]$NdkTarget,
        [string]$BindgenTriple
    )

    $lower = $Target.Replace("-", "_")
    $upper = $lower.ToUpperInvariant()
    $clang = Join-Path $llvmBin "${NdkTarget}26-clang.cmd"
    $sysroot = (Join-Path $llvmRoot "sysroot").Replace("\", "/")
    $includeRoot = "$sysroot/usr/include"
    $targetInclude = "$includeRoot/$BindgenTriple"

    Set-Item -Path "Env:CC_$lower" -Value $clang
    Set-Item -Path "Env:CXX_$lower" -Value (Join-Path $llvmBin "${NdkTarget}26-clang++.cmd")
    Set-Item -Path "Env:AR_$lower" -Value (Join-Path $llvmBin "llvm-ar.exe")
    Set-Item -Path "Env:CARGO_TARGET_${upper}_LINKER" -Value $clang
    Set-Item -Path "Env:BINDGEN_EXTRA_CLANG_ARGS_$lower" -Value "--target=${NdkTarget}26 --sysroot=$sysroot -I$includeRoot -I$targetInclude"
}

Push-Location $repoRoot
try {
    $env:KSU_PACKAGE_NAME = $ManagerPackage
    rustup target add aarch64-unknown-linux-musl aarch64-linux-android armv7-linux-androideabi x86_64-linux-android

    $env:CARGO_TARGET_AARCH64_UNKNOWN_LINUX_MUSL_LINKER = Join-Path $llvmBin "aarch64-linux-android26-clang.cmd"
    $env:RUSTFLAGS = "-C link-arg=-no-pie"
    cargo build --package ksuinit --target aarch64-unknown-linux-musl --release
    if ($LASTEXITCODE -ne 0) { throw "ksuinit build failed" }
    Remove-Item Env:RUSTFLAGS

    $uniqueExpectedKmis = @($ExpectedKmis | Sort-Object -Unique)
    if ($ExpectedKmis.Count -eq 0 -or $uniqueExpectedKmis.Count -ne $ExpectedKmis.Count) {
        throw "ExpectedKmis must contain at least one unique KMI name"
    }

    $expectedLkmNames = @($ExpectedKmis | ForEach-Object { "${_}_kernelsu.ko" })
    $lkmFiles = @(Get-ChildItem -LiteralPath $LkmDirectory -File -Filter "*_kernelsu.ko")
    $actualLkmNames = @($lkmFiles | ForEach-Object { $_.Name })
    $missingLkms = @($expectedLkmNames | Where-Object { $_ -notin $actualLkmNames })
    $unexpectedLkms = @($actualLkmNames | Where-Object { $_ -notin $expectedLkmNames })
    if ($missingLkms.Count -gt 0 -or $unexpectedLkms.Count -gt 0) {
        throw "KMI module set mismatch. Missing: [$($missingLkms -join ', ')]; unexpected: [$($unexpectedLkms -join ', ')]"
    }

    New-Item -ItemType Directory -Path $assetDirectory -Force | Out-Null
    Get-ChildItem -LiteralPath $assetDirectory -Filter "*_kernelsu.ko" | Remove-Item -Force
    Copy-Item -LiteralPath $ksuinit -Destination (Join-Path $assetDirectory "ksuinit") -Force
    $lkmFiles | Copy-Item -Destination $assetDirectory -Force

    $env:LIBCLANG_PATH = $llvmBin
    cargo clean --package ksud

    $targets = @(
        @{ Rust = "aarch64-linux-android"; Ndk = "aarch64-linux-android"; Bindgen = "aarch64-linux-android"; Abi = "arm64-v8a" },
        @{ Rust = "armv7-linux-androideabi"; Ndk = "armv7a-linux-androideabi"; Bindgen = "arm-linux-androideabi"; Abi = "armeabi-v7a" },
        @{ Rust = "x86_64-linux-android"; Ndk = "x86_64-linux-android"; Bindgen = "x86_64-linux-android"; Abi = "x86_64" }
    )

    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
    foreach ($target in $targets) {
        Set-TargetEnvironment -Target $target.Rust -NdkTarget $target.Ndk -BindgenTriple $target.Bindgen
        cargo build --target $target.Rust --release --manifest-path userspace\ksud\Cargo.toml
        if ($LASTEXITCODE -ne 0) { throw "ksud build failed for $($target.Rust)" }

        $binary = Join-Path $repoRoot "target\$($target.Rust)\release\ksud"
        Copy-Item -LiteralPath $binary -Destination (Join-Path $outputDirectory "ksud-$($target.Rust)") -Force
        $jniLibAbiDirectory = Join-Path $jniLibDirectory $target.Abi
        New-Item -ItemType Directory -Path $jniLibAbiDirectory -Force | Out-Null
        Copy-Item -LiteralPath $binary -Destination (Join-Path $jniLibAbiDirectory "libksud.so") -Force
    }
}
finally {
    Remove-Item Env:KSU_PACKAGE_NAME -ErrorAction SilentlyContinue
    Pop-Location
}
