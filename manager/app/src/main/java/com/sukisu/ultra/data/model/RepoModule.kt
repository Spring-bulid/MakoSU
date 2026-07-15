package com.sukisu.ultra.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Author(
    val name: String,
    val link: String,
)

@Immutable
data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val downloadCount: Int = 0,
)

@Immutable
data class RepoModule(
    val moduleId: String,
    val moduleName: String,
    val authors: String,
    val authorList: List<Author>,
    val summary: String,
    val metamodule: Boolean,
    val stargazerCount: Int,
    val latestRelease: String,
    val latestReleaseTime: String,
    val latestVersionCode: Long,
    val latestAsset: ReleaseAsset?,
    val repoUrl: String = "",
)
