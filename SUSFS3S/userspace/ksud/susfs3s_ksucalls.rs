}

/* SUSFS3S: LKM-native SuSFS subset (sus_path + sus_kstat), KSU_IOCTL_SUSFS3S */

/// Spoofed stat template for the ADD_SUS_KSTAT op. The kernel applies all
/// fields verbatim, so the template must be complete (e.g. a real stat of
/// the target with the fields to spoof edited).
#[derive(Debug, Default, Clone, Copy)]
pub struct Susfs3sKstat {
    pub st_ino: u64,
    pub st_size: u64,
    pub st_blksize: u64,
    pub st_blocks: u64,
    pub st_atime: i64,
    pub st_mtime: i64,
    pub st_ctime: i64,
    pub st_mode: u32,
    pub st_uid: u32,
    pub st_gid: u32,
    pub st_dev: u32,
    pub st_nlink: u32,
    pub st_rdev: u32,
}

fn susfs3s_cmd(op: u8, path: &str) -> anyhow::Result<ksu_uapi::ksu_susfs3s_cmd> {
    let path_bytes = path.as_bytes();
    if path_bytes.len() >= 256 {
        bail!("Path too long");
    }
    let mut cmd: ksu_uapi::ksu_susfs3s_cmd = unsafe { std::mem::zeroed() };
    cmd.op = op;
    for (dst, &src) in cmd.path.iter_mut().zip(path_bytes) {
        *dst = src as _;
    }
    Ok(cmd)
}

pub fn susfs3s_add_sus_path(path: &str) -> anyhow::Result<()> {
    let mut cmd = susfs3s_cmd(ksu_uapi::KSU_SUSFS3S_OP_ADD_SUS_PATH, path)?;
    ksuctl(ksu_uapi::KSU_IOCTL_SUSFS3S, &raw mut cmd)?;
    Ok(())
}

pub fn susfs3s_del_sus_path(path: &str) -> anyhow::Result<()> {
    let mut cmd = susfs3s_cmd(ksu_uapi::KSU_SUSFS3S_OP_DEL_SUS_PATH, path)?;
    ksuctl(ksu_uapi::KSU_IOCTL_SUSFS3S, &raw mut cmd)?;
    Ok(())
}

pub fn susfs3s_add_sus_kstat(path: &str, kstat: &Susfs3sKstat) -> anyhow::Result<()> {
    let mut cmd = susfs3s_cmd(ksu_uapi::KSU_SUSFS3S_OP_ADD_SUS_KSTAT, path)?;
    cmd.st_ino = kstat.st_ino;
    cmd.st_size = kstat.st_size;
    cmd.st_blksize = kstat.st_blksize;
    cmd.st_blocks = kstat.st_blocks;
    cmd.st_atime = kstat.st_atime;
    cmd.st_mtime = kstat.st_mtime;
    cmd.st_ctime = kstat.st_ctime;
    cmd.st_mode = kstat.st_mode;
    cmd.st_uid = kstat.st_uid;
    cmd.st_gid = kstat.st_gid;
    cmd.st_dev = kstat.st_dev;
    cmd.st_nlink = kstat.st_nlink;
    cmd.st_rdev = kstat.st_rdev;
    ksuctl(ksu_uapi::KSU_IOCTL_SUSFS3S, &raw mut cmd)?;
    Ok(())
}

pub fn susfs3s_del_sus_kstat(path: &str) -> anyhow::Result<()> {
    let mut cmd = susfs3s_cmd(ksu_uapi::KSU_SUSFS3S_OP_DEL_SUS_KSTAT, path)?;
    ksuctl(ksu_uapi::KSU_IOCTL_SUSFS3S, &raw mut cmd)?;
    Ok(())
}

/// Wipe both the sus_path and sus_kstat lists
pub fn susfs3s_wipe() -> anyhow::Result<()> {
