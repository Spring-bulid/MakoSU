package com.sukisu.ultra.ui.util

import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.util.module.LatestVersionInfo
import okhttp3.Request

private const val MAKOSU_RELEASES_API =
    "https://api.github.com/repos/Spring-bulid/MakoSU/releases/latest"
private val MAKOSU_RELEASE_APK = Regex("^MakoSU_v(.+?)_(\\d+)-release\\.apk$")

/**
 * @author weishu
 * @date 2023/6/22.
 */
suspend fun download(
    url: String,
    fileName: String,
    onDownloaded: (Uri) -> Unit = {},
    onDownloading: () -> Unit = {},
    onProgress: (Int) -> Unit = {}
) {
    onDownloading()

    val downloadId = DownloadManager.enqueue(
        context = ksuApp,
        url = url,
        fileName = fileName,
        onCompleted = onDownloaded,
    )

    DownloadManager.downloads
        .onEach { map -> map[downloadId]?.let { onProgress(it.progress) } }
        .first { map ->
            val status = map[downloadId]?.status
            status == DownloadManager.Status.COMPLETED ||
                status == DownloadManager.Status.FAILED
        }
}

fun checkNewVersion(): LatestVersionInfo {
    if (!isNetworkAvailable(ksuApp)) return LatestVersionInfo()
    // default null value if failed
    val defaultValue = LatestVersionInfo()
    runCatching {
        ksuApp.okhttpClient.newCall(
            Request.Builder()
                .url(MAKOSU_RELEASES_API)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "MakoSU-Manager")
                .build()
        ).execute()
            .use { response ->
                if (!response.isSuccessful) {
                    return defaultValue
                }
                val body = response.body.string()
                val json = org.json.JSONObject(body)
                val changelog = json.optString("body")

                val assets = json.optJSONArray("assets") ?: return defaultValue
                var latest: LatestVersionInfo? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val matchResult = MAKOSU_RELEASE_APK.matchEntire(asset.optString("name"))
                        ?: continue
                    val versionCode = matchResult.groupValues[2].toLongOrNull() ?: continue
                    val downloadUrl = asset.optString("browser_download_url")
                    if (downloadUrl.isBlank()) continue

                    val candidate = LatestVersionInfo(versionCode, downloadUrl, changelog)
                    if (candidate.versionCode > (latest?.versionCode ?: Long.MIN_VALUE)) {
                        latest = candidate
                    }
                }
                return latest ?: defaultValue

            }
    }
    return defaultValue
}
