package com.sukisu.ultra.ui.screen.home

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.R
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.magica.MagicaService
import com.sukisu.ultra.ui.LocalMainPagerState
import com.sukisu.ultra.ui.component.dialog.rememberLoadingDialog
import com.sukisu.ultra.ui.navigation3.Navigator
import com.sukisu.ultra.ui.navigation3.Route
import com.sukisu.ultra.ui.theme.UiRefresh
import com.sukisu.ultra.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomePager(
    navigator: Navigator,
    bottomInnerPadding: Dp,
    isCurrentPage: Boolean = true,
) {
    val viewModel = viewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainState = LocalMainPagerState.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val loadingDialog = rememberLoadingDialog()
    val scope = rememberCoroutineScope()

    val prefs = remember {
        ksuApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    /**
     * FolkPatch Home.kt pattern:
     *   var homeLayout by remember { prefs... }
     *   if (refreshThemeObserver) homeLayout = prefs...
     *
     * Layout is local UI state re-read only from prefs on explicit refresh —
     * never let async refresh()/extra listeners race and swap to another skin.
     */
    fun readLayout(): HomeLayout =
        HomeLayout.fromValue(prefs.getString("home_layout", HomeLayout.DEFAULT_VALUE))

    var homeLayout by remember { mutableStateOf(readLayout()) }
    val refreshToken by UiRefresh.token.collectAsStateWithLifecycle()

    LaunchedEffect(refreshToken) {
        // token 0 is initial; still sync once so cold start matches disk
        homeLayout = readLayout()
    }
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            homeLayout = readLayout()
        }
    }

    var hasActivated by remember { mutableStateOf(false) }
    if (isCurrentPage) hasActivated = true
    if (hasActivated) {
        LaunchedEffect(Unit) {
            // Data only — must not override homeLayout (see HomeViewModel.refresh)
            viewModel.refresh()
        }
    }

    val actions = HomeActions(
        onInstallClick = { navigator.push(Route.Install()) },
        onSuperuserClick = { if (!uiState.showRequireKernelWarning) mainState.animateToPage(1) },
        onModuleClick = { if (!uiState.showRequireKernelWarning) mainState.animateToPage(2) },
        onOpenUrl = uriHandler::openUri,
        onJailbreakClick = {
            loadingDialog.showLoading()
            context.startService(Intent(context, MagicaService::class.java))
            scope.launch(Dispatchers.IO) {
                delay(30_000.milliseconds)
                withContext(Dispatchers.Main) {
                    loadingDialog.hide()
                    Toast.makeText(context, R.string.jailbreak_timeout, Toast.LENGTH_LONG).show()
                }
            }
        },
    )

    // Force layout from local prefs-backed state (authoritative for UI)
    HomePagerMaterial(
        state = uiState.copy(homeLayout = homeLayout),
        actions = actions,
        bottomInnerPadding = bottomInnerPadding,
    )
}
