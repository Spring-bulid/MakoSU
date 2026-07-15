package com.sukisu.ultra.ui.screen.modulerepo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReleaseAssetArg(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val downloadCount: Int
) : Parcelable

@Parcelize
data class ReleaseArg(
    val tagName: String,
    val name: String,
    val publishedAt: String,
    val assets: List<ReleaseAssetArg>,
    val descriptionHTML: String
) : Parcelable

@Parcelize
data class AuthorArg(
    val name: String,
    val link: String,
) : Parcelable

@Parcelize
data class RepoModuleArg(
    val moduleId: String,
    val moduleName: String,
    val authors: String,
    val authorsList: List<AuthorArg>,
    val summary: String = "",
    val latestRelease: String,
    val latestReleaseTime: String,
    val downloadUrl: String = "",
    val repoUrl: String = "",
    val releases: List<ReleaseArg> = emptyList(),
) : Parcelable
