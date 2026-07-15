use std::{env, fs, process::Command};

fn verify_embedded_scripts() {
    let installer = fs::read("src/installer.sh").expect("Failed to read embedded installer.sh");
    assert!(
        !installer.contains(&b'\r'),
        "src/installer.sh must use LF line endings for Android sh"
    );
    println!("cargo:rerun-if-changed=src/installer.sh");
}

fn get_git_version() -> Result<(u32, String), std::io::Error> {
    let output = Command::new("git")
        .args(["rev-list", "--count", "HEAD"])
        .output()?;

    let output = output.stdout;
    let version_code = String::from_utf8(output).expect("Failed to read git count stdout");
    let version_code: u32 = version_code
        .trim()
        .parse()
        .map_err(|_| std::io::Error::other("Failed to parse git count"))?;
    let version_code = 40000 - 2815 + version_code; // For historical reasons

    let version_name = String::from_utf8(
        Command::new("git")
            .args(["describe", "--tags", "--always"])
            .output()?
            .stdout,
    )
    .map_err(|_| std::io::Error::other("Failed to parse git count"))?;
    let version_name = version_name.trim_start_matches('v').to_string();
    Ok((version_code, version_name))
}

fn configure_bindgen() {
    // The bindgen::Builder is the main entry point
    // to bindgen, and lets you build up options for
    // the resulting bindings.
    let bindings = bindgen::Builder::default()
        // The input header we would like to generate
        // bindings for.
        .header("src/ksu_uapi.h")
        .clang_args(["-x", "c++", "-I../../"])
        // Tell cargo to invalidate the built crate whenever any of the
        // included header files changed.
        .parse_callbacks(Box::new(bindgen::CargoCallbacks::new()))
        // Finish the builder and generate the bindings.
        .generate()
        // Unwrap the Result and panic on failure.
        .expect("Unable to generate bindings");

    // Write the bindings to the $OUT_DIR/bindings.rs file.
    let out_path = std::path::PathBuf::from(env::var("OUT_DIR").unwrap());
    // for debug, uncomment below
    // let out_path = std::path::PathBuf::from(env::var("CARGO_MANIFEST_DIR").unwrap());
    bindings
        .write_to_file(out_path.join("bindings.rs"))
        .expect("Couldn't write bindings!");
}

fn main() {
    verify_embedded_scripts();
    let (code, name) = match get_git_version() {
        Ok((code, name)) => (code, name),
        Err(_) => {
            // show warning if git is not installed
            println!("cargo:warning=Failed to get git version, using 0.0.0");
            (0, "0.0.0".to_string())
        }
    };
    println!("cargo:rerun-if-env-changed=KSU_PACKAGE_NAME");
    let package_name =
        env::var("KSU_PACKAGE_NAME").unwrap_or_else(|_| "com.makosu.manager".to_string());
    println!("cargo:rustc-env=KSU_PACKAGE_NAME={package_name}");
    println!("cargo:rustc-env=VERSION_CODE={code}");
    println!("cargo:rustc-env=VERSION_NAME={name}");

    let target_os = env::var("CARGO_CFG_TARGET_OS").expect("CARGO_CFG_TARGET_OS not set");
    if target_os == "android" {
        configure_bindgen();
    }
}
