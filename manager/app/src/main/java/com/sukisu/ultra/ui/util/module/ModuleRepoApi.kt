package com.sukisu.ultra.ui.util.module

import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.util.isNetworkAvailable
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

private const val MODULES_URL =
    "https://gitee.com/JT22/MakoSU_ModuleDownload/raw/main/modules.json"

data class ModuleDetail(
    val readme: String,
    val readmeHtml: String,
    val latestTag: String,
    val latestTime: String,
    val latestAssetName: String?,
    val latestAssetUrl: String?,
    val releases: List<ReleaseInfo>,
    val homepageUrl: String,
    val sourceUrl: String,
    val url: String,
)

data class ReleaseInfo(
    val name: String,
    val tagName: String,
    val publishedAt: String,
    val descriptionHTML: String,
    val assets: List<ReleaseAssetInfo>,
)

data class ReleaseAssetInfo(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val downloadCount: Int,
)

fun sanitizeVersionString(version: String): String {
    return version.replace(Regex("[^a-zA-Z0-9.\\-_]"), "_")
}

fun stripTicks(s: String): String {
    val t = s.trim()
    return if (t.startsWith("`") && t.endsWith("`") && t.length >= 2) {
        t.substring(1, t.length - 1)
    } else {
        t
    }
}

private fun fetchCatalogArray(): JSONArray? {
    if (!isNetworkAvailable(ksuApp)) return null
    return runCatching {
        ksuApp.okhttpClient.newCall(Request.Builder().url(MODULES_URL).build()).execute().use { resp ->
            if (!resp.isSuccessful) null else JSONArray(resp.body.string())
        }
    }.getOrNull()
}

private fun findCatalogModule(moduleId: String): JSONObject? {
    val array = fetchCatalogArray() ?: return null
    for (i in 0 until array.length()) {
        val item = array.optJSONObject(i) ?: continue
        if (item.optString("moduleId", "") == moduleId) {
            return item
        }
    }
    return null
}

private fun parseModuleDetail(item: JSONObject): ModuleDetail {
    val summary = item.optString("summary", "")
    val repoUrl = stripTicks(item.optString("repoUrl", ""))
    val lr = item.optJSONObject("latestRelease")
    val latestTag = lr?.optString("name", lr.optString("version", ""))
        ?: item.optString("latestRelease", "")
    val latestTime = lr?.optString("time", "") ?: ""
    val downloadUrl = stripTicks(lr?.optString("downloadUrl", "") ?: "")
    val assetName = downloadUrl.substringAfterLast('/').takeIf { it.isNotEmpty() }
    val assetSize = lr?.optLong("size", 0L) ?: 0L
    val assetDownloads = lr?.optInt("downloadCount", 0) ?: 0
    val releases = if (downloadUrl.isNotEmpty()) {
        listOf(
            ReleaseInfo(
                name = latestTag,
                tagName = latestTag,
                publishedAt = latestTime,
                descriptionHTML = summary,
                assets = listOf(
                    ReleaseAssetInfo(
                        name = assetName ?: "${item.optString("moduleId", "module")}.zip",
                        downloadUrl = downloadUrl,
                        size = assetSize,
                        downloadCount = assetDownloads,
                    )
                ),
            )
        )
    } else {
        emptyList()
    }

    return ModuleDetail(
        readme = summary,
        readmeHtml = summary,
        latestTag = latestTag,
        latestTime = latestTime,
        latestAssetName = assetName,
        latestAssetUrl = downloadUrl.ifEmpty { null },
        releases = releases,
        homepageUrl = repoUrl,
        sourceUrl = repoUrl,
        url = repoUrl,
    )
}

fun fetchReleaseDescriptionHtml(moduleId: String, latestTag: String): String? {
    val item = findCatalogModule(moduleId) ?: return null
    val detail = parseModuleDetail(item)
    if (latestTag.isBlank() || detail.latestTag == latestTag || detail.latestTag.isBlank()) {
        return detail.readmeHtml.takeIf { it.isNotBlank() }
    }
    return detail.releases
        .firstOrNull { it.tagName == latestTag || it.name == latestTag }
        ?.descriptionHTML
        ?.takeIf { it.isNotBlank() }
        ?: detail.readmeHtml.takeIf { it.isNotBlank() }
}

fun fetchModuleDetail(moduleId: String): ModuleDetail? {
    if (moduleId.isBlank()) return null
    val item = findCatalogModule(moduleId) ?: return null
    return parseModuleDetail(item)
}
