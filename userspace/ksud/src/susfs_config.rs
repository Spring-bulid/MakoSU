//! SuSFS persistent configuration store.
//!
//! Settings are stored in `/data/adb/ksu/susfs_config` using a small
//! length-prefixed binary format. Updates are serialized across processes and
//! committed with an atomic rename so a crash cannot truncate the live file.

use anyhow::{Context, Result, bail};
#[cfg(target_os = "android")]
use const_format::concatcp;
use std::collections::{BTreeMap, HashMap};
#[cfg(unix)]
use std::fs::File;
use std::fs::OpenOptions;
use std::io::Write;
use std::path::Path;
#[cfg(not(target_os = "android"))]
use std::sync::{Mutex, MutexGuard};

#[cfg(target_os = "android")]
use std::os::fd::AsRawFd;
#[cfg(unix)]
use std::os::unix::fs::OpenOptionsExt;

const SUSFS_CONFIG_MAGIC: u32 = 0x5355_5346; // "SUSF"
const SUSFS_CONFIG_VERSION: u32 = 1;
#[cfg(target_os = "android")]
const SUSFS_CONFIG_FILE: &str = concatcp!(crate::defs::WORKING_DIR, "susfs_config");
#[cfg(not(target_os = "android"))]
const SUSFS_CONFIG_FILE: &str = "susfs_config.test";
#[cfg(target_os = "android")]
const SUSFS_CONFIG_LOCK_FILE: &str = concatcp!(crate::defs::WORKING_DIR, "susfs_config.lock");
const MAX_CONFIG_BYTES: usize = 4 * 1024 * 1024;
const MAX_CONFIG_ENTRIES: usize = 256;
const MAX_KEY_BYTES: usize = 256;
const MAX_VALUE_BYTES: usize = 1024 * 1024;

#[cfg(not(target_os = "android"))]
static CONFIG_MUTEX: Mutex<()> = Mutex::new(());

struct ConfigLock {
    #[cfg(target_os = "android")]
    _file: File,
    #[cfg(not(target_os = "android"))]
    _guard: MutexGuard<'static, ()>,
}

// These keys must match SuSFSConfig.kt.
pub const KEY_UNAME_VALUE: &str = "uname_value";
pub const KEY_BUILD_TIME_VALUE: &str = "build_time_value";
pub const KEY_AUTO_START_ENABLED: &str = "auto_start_enabled";
pub const KEY_SUS_PATHS: &str = "sus_paths";
pub const KEY_SUS_LOOP_PATHS: &str = "sus_loop_paths";
pub const KEY_SUS_MAPS: &str = "sus_maps";
pub const KEY_ENABLE_LOG: &str = "enable_log";
pub const KEY_EXECUTE_IN_POST_FS_DATA: &str = "execute_in_post_fs_data";
pub const KEY_KSTAT_CONFIGS: &str = "kstat_configs";
pub const KEY_ADD_KSTAT_PATHS: &str = "add_kstat_paths";
pub const KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS: &str = "hide_sus_mounts_for_all_procs";
pub const KEY_ENABLE_CLEANUP_RESIDUE: &str = "enable_cleanup_residue";
pub const KEY_ENABLE_HIDE_BL: &str = "enable_hide_bl";
pub const KEY_ENABLE_AVC_LOG_SPOOFING: &str = "enable_avc_log_spoofing";

pub const DEFAULT_UNAME: &str = "default";
pub const DEFAULT_BUILD_TIME: &str = "default";

#[allow(dead_code)]
pub const ALL_KEYS: &[&str] = &[
    KEY_UNAME_VALUE,
    KEY_BUILD_TIME_VALUE,
    KEY_AUTO_START_ENABLED,
    KEY_SUS_PATHS,
    KEY_SUS_LOOP_PATHS,
    KEY_SUS_MAPS,
    KEY_ENABLE_LOG,
    KEY_EXECUTE_IN_POST_FS_DATA,
    KEY_KSTAT_CONFIGS,
    KEY_ADD_KSTAT_PATHS,
    KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS,
    KEY_ENABLE_CLEANUP_RESIDUE,
    KEY_ENABLE_HIDE_BL,
    KEY_ENABLE_AVC_LOG_SPOOFING,
];

fn config_path() -> &'static Path {
    Path::new(SUSFS_CONFIG_FILE)
}

fn ensure_parent_dir(path: &Path) -> Result<()> {
    if let Some(parent) = path.parent() {
        std::fs::create_dir_all(parent).context("Failed to create config directory")?;
    }
    Ok(())
}

fn acquire_config_lock() -> Result<ConfigLock> {
    #[cfg(target_os = "android")]
    {
        let lock_path = Path::new(SUSFS_CONFIG_LOCK_FILE);
        ensure_parent_dir(lock_path)?;
        let file = OpenOptions::new()
            .read(true)
            .write(true)
            .create(true)
            .truncate(false)
            .mode(0o600)
            .open(lock_path)
            .context("Failed to open config lock file")?;

        loop {
            if unsafe { libc::flock(file.as_raw_fd(), libc::LOCK_EX) } == 0 {
                return Ok(ConfigLock { _file: file });
            }
            let error = std::io::Error::last_os_error();
            if error.kind() != std::io::ErrorKind::Interrupted {
                return Err(error).context("Failed to lock SuSFS config");
            }
        }
    }

    #[cfg(not(target_os = "android"))]
    {
        let guard = CONFIG_MUTEX
            .lock()
            .map_err(|_| anyhow::anyhow!("SuSFS config lock is poisoned"))?;
        Ok(ConfigLock { _guard: guard })
    }
}

fn validate_entry(key: &str, value: &str) -> Result<()> {
    if key.is_empty() {
        bail!("Config key must not be empty");
    }
    if key.len() > MAX_KEY_BYTES {
        bail!("Config key exceeds {MAX_KEY_BYTES} bytes");
    }
    if value.len() > MAX_VALUE_BYTES {
        bail!("Config value for {key:?} exceeds {MAX_VALUE_BYTES} bytes");
    }
    Ok(())
}

fn write_binary(config: &HashMap<String, String>) -> Result<Vec<u8>> {
    if config.len() > MAX_CONFIG_ENTRIES {
        bail!("Config contains too many entries: {}", config.len());
    }

    let mut buffer = Vec::new();
    buffer.extend_from_slice(&SUSFS_CONFIG_MAGIC.to_le_bytes());
    buffer.extend_from_slice(&SUSFS_CONFIG_VERSION.to_le_bytes());
    buffer.extend_from_slice(&(config.len() as u32).to_le_bytes());

    let mut entries: Vec<_> = config.iter().collect();
    entries.sort_unstable_by_key(|(key, _)| *key);

    for (key, value) in entries {
        validate_entry(key, value)?;
        buffer.extend_from_slice(&(key.len() as u32).to_le_bytes());
        buffer.extend_from_slice(key.as_bytes());
        buffer.extend_from_slice(&(value.len() as u32).to_le_bytes());
        buffer.extend_from_slice(value.as_bytes());
    }

    if buffer.len() > MAX_CONFIG_BYTES {
        bail!("Serialized config exceeds {MAX_CONFIG_BYTES} bytes");
    }
    Ok(buffer)
}

fn read_u32(data: &mut &[u8], field: &str) -> Result<u32> {
    let bytes = data
        .get(..4)
        .with_context(|| format!("Truncated {field}"))?;
    *data = &data[4..];
    Ok(u32::from_le_bytes(bytes.try_into().expect("four bytes")))
}

fn read_bytes<'a>(data: &mut &'a [u8], len: usize, field: &str) -> Result<&'a [u8]> {
    let bytes = data
        .get(..len)
        .with_context(|| format!("Truncated {field}"))?;
    *data = &data[len..];
    Ok(bytes)
}

fn read_binary(data: &[u8]) -> Result<HashMap<String, String>> {
    if data.len() > MAX_CONFIG_BYTES {
        bail!("Config file exceeds {MAX_CONFIG_BYTES} bytes");
    }

    let mut remaining = data;
    let mut config = HashMap::new();

    let magic = read_u32(&mut remaining, "magic")?;
    if magic != SUSFS_CONFIG_MAGIC {
        bail!("Invalid magic: expected 0x{SUSFS_CONFIG_MAGIC:08x}, got 0x{magic:08x}");
    }

    let version = read_u32(&mut remaining, "version")?;
    if version != SUSFS_CONFIG_VERSION {
        bail!("Unsupported version: expected {SUSFS_CONFIG_VERSION}, got {version}");
    }

    let count = read_u32(&mut remaining, "entry count")? as usize;
    if count > MAX_CONFIG_ENTRIES {
        bail!("Config contains too many entries: {count}");
    }

    for _ in 0..count {
        let key_len = read_u32(&mut remaining, "key length")? as usize;
        if key_len == 0 || key_len > MAX_KEY_BYTES {
            bail!("Invalid config key length: {key_len}");
        }
        let key = std::str::from_utf8(read_bytes(&mut remaining, key_len, "key data")?)
            .context("Invalid UTF-8 in key")?
            .to_owned();

        let value_len = read_u32(&mut remaining, "value length")? as usize;
        if value_len > MAX_VALUE_BYTES {
            bail!("Config value for {key:?} exceeds {MAX_VALUE_BYTES} bytes");
        }
        let value = std::str::from_utf8(read_bytes(&mut remaining, value_len, "value data")?)
            .context("Invalid UTF-8 in value")?
            .to_owned();

        if config.insert(key.clone(), value).is_some() {
            bail!("Duplicate config key: {key}");
        }
    }

    if !remaining.is_empty() {
        bail!("Unexpected trailing data: {} bytes", remaining.len());
    }
    Ok(config)
}

fn load_config_unlocked(path: &Path) -> Result<HashMap<String, String>> {
    if !path.exists() {
        return Ok(HashMap::new());
    }
    let data = std::fs::read(path).context("Failed to read config file")?;
    read_binary(&data)
}

#[cfg(unix)]
fn sync_parent(path: &Path) -> Result<()> {
    if let Some(parent) = path.parent() {
        File::open(parent)
            .and_then(|directory| directory.sync_all())
            .context("Failed to sync config directory")?;
    }
    Ok(())
}

#[cfg(not(unix))]
fn sync_parent(_: &Path) -> Result<()> {
    Ok(())
}

fn save_config_unlocked(path: &Path, config: &HashMap<String, String>) -> Result<()> {
    ensure_parent_dir(path)?;
    let data = write_binary(config)?;
    let parent = path
        .parent()
        .context("Config path has no parent directory")?;
    let temp_path = parent.join(".susfs_config.tmp");

    let mut options = OpenOptions::new();
    options.write(true).create(true).truncate(true);
    #[cfg(unix)]
    options.mode(0o600);

    let write_result = (|| -> Result<()> {
        let mut file = options
            .open(&temp_path)
            .context("Failed to open temporary config file")?;
        file.write_all(&data)
            .context("Failed to write temporary config file")?;
        file.sync_all()
            .context("Failed to sync temporary config file")?;
        drop(file);

        std::fs::rename(&temp_path, path).context("Failed to replace config file")?;
        sync_parent(path)
    })();

    if write_result.is_err() {
        let _ = std::fs::remove_file(&temp_path);
    }
    write_result
}

/// Load the stored config. Missing files produce an empty map.
pub fn load_config() -> Result<HashMap<String, String>> {
    let _lock = acquire_config_lock()?;
    load_config_unlocked(config_path())
}

/// Save the full config using an atomic replacement.
pub fn save_config(config: &HashMap<String, String>) -> Result<()> {
    let _lock = acquire_config_lock()?;
    save_config_unlocked(config_path(), config)
}

fn update_config(mutator: impl FnOnce(&mut HashMap<String, String>)) -> Result<()> {
    let _lock = acquire_config_lock()?;
    let path = config_path();
    let mut config = load_config_unlocked(path)?;
    mutator(&mut config);
    save_config_unlocked(path, &config)
}

/// Get one value. Missing keys produce an empty string.
pub fn get(key: &str) -> Result<String> {
    let config = load_config()?;
    Ok(config.get(key).cloned().unwrap_or_default())
}

/// Set one value without losing concurrent updates to other keys.
pub fn set(key: &str, value: &str) -> Result<()> {
    validate_entry(key, value)?;
    update_config(|config| {
        config.insert(key.to_owned(), value.to_owned());
    })
}

pub fn remove(key: &str) -> Result<()> {
    update_config(|config| {
        config.remove(key);
    })
}

pub fn clear() -> Result<()> {
    let _lock = acquire_config_lock()?;
    save_config_unlocked(config_path(), &HashMap::new())
}

fn default_config() -> HashMap<String, String> {
    HashMap::from([
        (KEY_UNAME_VALUE.to_owned(), DEFAULT_UNAME.to_owned()),
        (
            KEY_BUILD_TIME_VALUE.to_owned(),
            DEFAULT_BUILD_TIME.to_owned(),
        ),
        (KEY_AUTO_START_ENABLED.to_owned(), "false".to_owned()),
        (KEY_SUS_PATHS.to_owned(), String::new()),
        (KEY_SUS_LOOP_PATHS.to_owned(), String::new()),
        (KEY_SUS_MAPS.to_owned(), String::new()),
        (KEY_ENABLE_LOG.to_owned(), "false".to_owned()),
        (KEY_EXECUTE_IN_POST_FS_DATA.to_owned(), "false".to_owned()),
        (KEY_KSTAT_CONFIGS.to_owned(), String::new()),
        (KEY_ADD_KSTAT_PATHS.to_owned(), String::new()),
        (
            KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS.to_owned(),
            "false".to_owned(),
        ),
        (KEY_ENABLE_CLEANUP_RESIDUE.to_owned(), "false".to_owned()),
        (KEY_ENABLE_HIDE_BL.to_owned(), "false".to_owned()),
        (KEY_ENABLE_AVC_LOG_SPOOFING.to_owned(), "false".to_owned()),
    ])
}

fn load_effective_config() -> Result<HashMap<String, String>> {
    let stored = load_config()?;
    let mut effective = default_config();
    effective.extend(stored);
    Ok(effective)
}

pub fn reset_to_defaults() -> Result<()> {
    save_config(&default_config())
}

/// Atomically replace the stored config from a JSON string map.
pub fn replace_from_json(json: &str) -> Result<()> {
    let config: HashMap<String, String> =
        serde_json::from_str(json).context("Invalid SuSFS config JSON")?;
    write_binary(&config)?;
    save_config(&config)
}

/// Export the effective config as deterministic `key=value` lines.
pub fn export_lines() -> Result<String> {
    let config = load_effective_config()?;
    let mut lines: Vec<_> = config
        .iter()
        .map(|(key, value)| format!("{key}={value}"))
        .collect();
    lines.sort_unstable();
    Ok(lines.join("\n"))
}

/// Export the effective config as deterministic JSON for one-shot UI reads.
pub fn export_json() -> Result<String> {
    let config = load_effective_config()?;
    let sorted: BTreeMap<_, _> = config.into_iter().collect();
    serde_json::to_string(&sorted).context("Failed to serialize SuSFS config")
}

fn split_paths(raw: &str) -> Vec<String> {
    raw.split(';')
        .filter(|value| !value.is_empty())
        .map(str::to_owned)
        .collect()
}

fn split_kstat_configs(raw: &str) -> Vec<String> {
    raw.split(";;")
        .filter(|value| !value.is_empty())
        .map(str::to_owned)
        .collect()
}

/// Load the effective settings used to generate the auto-start module.
pub fn load_module_config() -> Result<ModuleConfig> {
    let config = load_effective_config()?;
    let get = |key: &str| config.get(key).cloned().unwrap_or_default();

    Ok(ModuleConfig {
        uname_value: get(KEY_UNAME_VALUE),
        build_time_value: get(KEY_BUILD_TIME_VALUE),
        execute_in_post_fs_data: get(KEY_EXECUTE_IN_POST_FS_DATA) == "true",
        sus_paths: split_paths(&get(KEY_SUS_PATHS)),
        sus_loop_paths: split_paths(&get(KEY_SUS_LOOP_PATHS)),
        sus_maps: split_paths(&get(KEY_SUS_MAPS)),
        enable_log: get(KEY_ENABLE_LOG) == "true",
        kstat_configs: split_kstat_configs(&get(KEY_KSTAT_CONFIGS)),
        add_kstat_paths: split_paths(&get(KEY_ADD_KSTAT_PATHS)),
        hide_sus_mounts_for_all_procs: get(KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS) == "true",
        enable_hide_bl: get(KEY_ENABLE_HIDE_BL) == "true",
        enable_cleanup_residue: get(KEY_ENABLE_CLEANUP_RESIDUE) == "true",
        enable_avc_log_spoofing: get(KEY_ENABLE_AVC_LOG_SPOOFING) == "true",
    })
}

#[derive(Debug, Clone)]
#[allow(clippy::struct_excessive_bools)]
pub struct ModuleConfig {
    pub uname_value: String,
    pub build_time_value: String,
    pub execute_in_post_fs_data: bool,
    pub sus_paths: Vec<String>,
    pub sus_loop_paths: Vec<String>,
    pub sus_maps: Vec<String>,
    pub enable_log: bool,
    pub kstat_configs: Vec<String>,
    pub add_kstat_paths: Vec<String>,
    pub hide_sus_mounts_for_all_procs: bool,
    pub enable_hide_bl: bool,
    pub enable_cleanup_residue: bool,
    pub enable_avc_log_spoofing: bool,
}

#[cfg(test)]
mod tests {
    use super::*;

    fn sample_config() -> HashMap<String, String> {
        HashMap::from([
            (KEY_UNAME_VALUE.to_owned(), "6.6.0-test".to_owned()),
            (KEY_SUS_PATHS.to_owned(), "/data/a;/data/b".to_owned()),
            ("unicode".to_owned(), "test-value".to_owned()),
        ])
    }

    #[test]
    fn binary_config_round_trip_preserves_values() {
        let config = sample_config();
        assert_eq!(
            read_binary(&write_binary(&config).unwrap()).unwrap(),
            config
        );
    }

    #[test]
    fn binary_output_is_deterministic() {
        let mut first = HashMap::new();
        first.insert("z".to_owned(), "last".to_owned());
        first.insert("a".to_owned(), "first".to_owned());

        let mut second = HashMap::new();
        second.insert("a".to_owned(), "first".to_owned());
        second.insert("z".to_owned(), "last".to_owned());

        assert_eq!(
            write_binary(&first).unwrap(),
            write_binary(&second).unwrap()
        );
    }

    #[test]
    fn parser_rejects_every_truncated_prefix() {
        let data = write_binary(&sample_config()).unwrap();
        for end in 0..data.len() {
            assert!(read_binary(&data[..end]).is_err(), "accepted prefix {end}");
        }
    }

    #[test]
    fn parser_rejects_duplicate_keys() {
        let data = write_binary(&HashMap::from([("key".to_owned(), "value".to_owned())])).unwrap();
        let mut duplicate = data[..8].to_vec();
        duplicate.extend_from_slice(&2_u32.to_le_bytes());
        duplicate.extend_from_slice(&data[12..]);
        duplicate.extend_from_slice(&data[12..]);

        assert!(read_binary(&duplicate).is_err());
    }

    #[test]
    fn parser_rejects_trailing_data_and_oversized_fields() {
        let mut trailing = write_binary(&sample_config()).unwrap();
        trailing.push(0);
        assert!(read_binary(&trailing).is_err());

        let mut oversized_key = Vec::new();
        oversized_key.extend_from_slice(&SUSFS_CONFIG_MAGIC.to_le_bytes());
        oversized_key.extend_from_slice(&SUSFS_CONFIG_VERSION.to_le_bytes());
        oversized_key.extend_from_slice(&1_u32.to_le_bytes());
        oversized_key.extend_from_slice(&((MAX_KEY_BYTES + 1) as u32).to_le_bytes());
        assert!(read_binary(&oversized_key).is_err());

        let mut excessive_count = Vec::new();
        excessive_count.extend_from_slice(&SUSFS_CONFIG_MAGIC.to_le_bytes());
        excessive_count.extend_from_slice(&SUSFS_CONFIG_VERSION.to_le_bytes());
        excessive_count.extend_from_slice(&((MAX_CONFIG_ENTRIES + 1) as u32).to_le_bytes());
        assert!(read_binary(&excessive_count).is_err());
    }

    #[test]
    fn default_config_is_complete_and_uses_safe_boolean_defaults() {
        let config = default_config();

        assert_eq!(config.len(), ALL_KEYS.len());
        assert!(ALL_KEYS.iter().all(|key| config.contains_key(*key)));
        assert_eq!(config[KEY_UNAME_VALUE], DEFAULT_UNAME);
        assert_eq!(config[KEY_BUILD_TIME_VALUE], DEFAULT_BUILD_TIME);
        assert_eq!(config[KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS], "false");
        assert_eq!(config[KEY_ENABLE_HIDE_BL], "false");
        assert_eq!(config[KEY_ENABLE_CLEANUP_RESIDUE], "false");
        assert_eq!(config[KEY_ENABLE_AVC_LOG_SPOOFING], "false");
    }
}
