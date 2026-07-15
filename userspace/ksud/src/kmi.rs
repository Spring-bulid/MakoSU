use anyhow::{Context, Result, anyhow};
use regex_lite::Regex;

pub const fn is_gki_version(major: i32, minor: i32) -> bool {
    major > 5 || major == 5 && minor >= 10
}

pub fn parse_kmi_version(version: &str) -> Result<String> {
    let re = Regex::new(r"(\d+\.\d+)(?:\S+)?(android\d+)")?;
    let captures = re
        .captures(version)
        .ok_or_else(|| anyhow!("Failed to get KMI from boot/modules"))?;
    let kernel_version = captures.get(1).map_or("", |value| value.as_str());
    let android_version = captures.get(2).map_or("", |value| value.as_str());
    Ok(format!("{android_version}-{kernel_version}"))
}

pub fn parse_kmi_from_bytes(buffer: &[u8]) -> Result<String> {
    let re =
        Regex::new(r"(\d+\.\d+)(?:\S+)?(android\d+)").context("Failed to compile KMI regex")?;

    buffer
        .windows(4)
        .enumerate()
        .filter(|(_, candidate)| {
            candidate[1] == b'.'
                && candidate[2].is_ascii_digit()
                && match candidate[0] {
                    b'5' => candidate[2] == b'4' || candidate[3].is_ascii_digit(),
                    b'6'..=b'9' => true,
                    _ => false,
                }
        })
        .find_map(|(offset, _)| {
            let candidate = &buffer[offset..buffer.len().min(offset + 100)];
            let end = candidate.iter().position(|byte| *byte == 0)?;
            let version = std::str::from_utf8(&candidate[..end]).ok()?;
            let captures = re.captures(version)?;
            let kernel_version = captures.get(1)?;
            let android_version = captures.get(2)?;
            Some(format!(
                "{}-{}",
                android_version.as_str(),
                kernel_version.as_str()
            ))
        })
        .ok_or_else(|| {
            println!("- Failed to get KMI version");
            anyhow!("Try to choose LKM manually")
        })
}

#[cfg(test)]
mod tests {
    use super::{is_gki_version, parse_kmi_from_bytes, parse_kmi_version};

    #[test]
    fn recognizes_supported_gki_generations() {
        assert!(!is_gki_version(5, 4));
        assert!(is_gki_version(5, 10));
        assert!(is_gki_version(5, 15));
        assert!(is_gki_version(6, 1));
        assert!(!is_gki_version(4, 19));
        assert!(!is_gki_version(5, 9));
    }

    #[test]
    fn parses_android11_gki_1_version() {
        let image = b"prefix\0Linux version 5.4.210-android11-9-g123456789abc\0suffix";
        assert_eq!(parse_kmi_from_bytes(image).unwrap(), "android11-5.4");
        assert_eq!(
            parse_kmi_version("vermagic: 5.4.210-android11-9-g123456789abc SMP").unwrap(),
            "android11-5.4"
        );
    }

    #[test]
    fn parses_android12_gki_2_version() {
        let image = b"prefix\0Linux version 5.10.218-android12-9-g123456789abc\0suffix";
        assert_eq!(parse_kmi_from_bytes(image).unwrap(), "android12-5.10");
    }

    #[test]
    fn rejects_version_without_android_kmi_marker() {
        let image = b"prefix\0Linux version 5.4.210-custom\0suffix";
        assert!(parse_kmi_from_bytes(image).is_err());
        assert!(parse_kmi_version("5.4.210-custom").is_err());
    }
}
