package com.sukisu.ultra.ui.screen.modulerepo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.navigation3.Route
import com.sukisu.ultra.ui.screen.flash.FlashIt
import com.sukisu.ultra.ui.viewmodel.ModuleRepoViewModel
import com.sukisu.ultra.ui.viewmodel.ModuleViewModel

@Composable
fun ModuleRepoScreen() {
    val navigator = LocalNavigator.current
    val viewModel = viewModel<ModuleRepoViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val installedVm = viewModel<ModuleViewModel>()
    val installedUiState by installedVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState.modules.isEmpty()) {
            viewModel.refresh()
        }
        if (installedUiState.moduleList.isEmpty()) {
            installedVm.fetchModuleList()
        }
    }

    val actions = ModuleRepoActions(
        onBack = { navigator.pop() },
        onRefresh = viewModel::refresh,
        onSearchTextChange = viewModel::updateSearchText,
        onClearSearch = { viewModel.updateSearchText("") },
        onSearchStatusChange = viewModel::updateSearchStatus,
        onSetSortOrder = viewModel::setSortOrder,
        onOpenRepoDetail = { module ->
            val downloadUrl = module.latestAsset?.downloadUrl.orEmpty()
            val assetName = module.latestAsset?.name
                ?: downloadUrl.substringAfterLast('/').ifBlank { "${module.moduleId}.zip" }
            val releaseName = module.latestRelease.ifBlank { assetName }
            val releases = if (downloadUrl.isNotEmpty()) {
                listOf(
                    ReleaseArg(
                        tagName = releaseName,
                        name = releaseName,
                        publishedAt = module.latestReleaseTime,
                        assets = listOf(
                            ReleaseAssetArg(
                                name = assetName,
                                downloadUrl = downloadUrl,
                                size = module.latestAsset?.size ?: 0L,
                                downloadCount = module.latestAsset?.downloadCount ?: 0,
                            )
                        ),
                        descriptionHTML = module.summary,
                    )
                )
            } else {
                emptyList()
            }
            val args = RepoModuleArg(
                moduleId = module.moduleId,
                moduleName = module.moduleName,
                authors = module.authors,
                authorsList = module.authorList.map { AuthorArg(it.name, it.link) },
                summary = module.summary,
                latestRelease = releaseName,
                latestReleaseTime = module.latestReleaseTime,
                downloadUrl = downloadUrl,
                repoUrl = module.repoUrl.ifBlank {
                    module.authorList.firstOrNull()?.link.orEmpty()
                },
                releases = releases,
            )
            navigator.push(Route.ModuleRepoDetail(args))
        },
    )

    ModuleRepoScreenMiuix(uiState, actions)
}

@Composable
fun ModuleRepoDetailScreen(module: RepoModuleArg) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val sourceUrl = module.repoUrl.ifBlank {
        module.authorsList.firstOrNull()?.link.orEmpty()
    }
    val webUrl = sourceUrl
    val detailReleases = module.releases.ifEmpty {
        if (module.downloadUrl.isNotEmpty()) {
            listOf(
                ReleaseArg(
                    tagName = module.latestRelease.ifBlank { module.moduleId },
                    name = module.latestRelease.ifBlank { module.moduleId },
                    publishedAt = module.latestReleaseTime,
                    assets = listOf(
                        ReleaseAssetArg(
                            name = module.downloadUrl.substringAfterLast('/')
                                .ifBlank { "${module.moduleId}.zip" },
                            downloadUrl = module.downloadUrl,
                            size = module.releases.firstOrNull()?.assets?.firstOrNull()?.size ?: 0L,
                            downloadCount = module.releases.firstOrNull()?.assets?.firstOrNull()?.downloadCount ?: 0,
                        )
                    ),
                    descriptionHTML = module.summary,
                )
            )
        } else {
            emptyList()
        }
    }
    val readmeHtml = module.summary.takeIf { it.isNotBlank() }

    val state = ModuleRepoDetailUiState(
        module = module,
        readmeHtml = readmeHtml,
        readmeLoaded = true,
        detailReleases = detailReleases,
        webUrl = webUrl,
        sourceUrl = sourceUrl,
    )
    val actions = ModuleRepoDetailActions(
        onBack = { navigator.pop() },
        onOpenWebUrl = { if (webUrl.isNotEmpty()) uriHandler.openUri(webUrl) },
        onOpenUrl = uriHandler::openUri,
        onInstallModule = { uri -> navigator.push(Route.Flash(FlashIt.FlashModules(listOf(uri)))) },
    )

    ModuleRepoDetailScreenMiuix(state, actions)
}
