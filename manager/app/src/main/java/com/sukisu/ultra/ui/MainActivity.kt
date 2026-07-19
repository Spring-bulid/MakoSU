package com.sukisu.ultra.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.channels.Channel
import com.sukisu.ultra.Natives
import com.sukisu.ultra.ui.component.bottombar.BottomBar
import com.sukisu.ultra.ui.component.CustomBackground
import com.sukisu.ultra.ui.component.bottombar.MainPagerState
import com.sukisu.ultra.ui.component.bottombar.SideRail
import com.sukisu.ultra.ui.component.bottombar.rememberMainPagerState
import com.sukisu.ultra.ui.kernelFlash.KernelFlashScreen
import com.sukisu.ultra.ui.navigation3.HandleZipFileIntent
import com.sukisu.ultra.ui.navigation3.IntentDispatcher
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.navigation3.Navigator
import com.sukisu.ultra.ui.navigation3.Route
import com.sukisu.ultra.ui.navigation3.rememberNavigator
import com.sukisu.ultra.ui.screen.about.AboutScreen
import com.sukisu.ultra.ui.screen.appprofile.AppProfileScreen
import com.sukisu.ultra.ui.screen.colorpalette.ColorPaletteScreen
import com.sukisu.ultra.ui.screen.executemoduleaction.ExecuteModuleActionScreen
import com.sukisu.ultra.ui.screen.flash.FlashScreen
import com.sukisu.ultra.ui.screen.home.HomePager
import com.sukisu.ultra.ui.screen.install.InstallScreen
import com.sukisu.ultra.ui.screen.kpm.KpmScreen
import com.sukisu.ultra.ui.screen.module.ModulePager
import com.sukisu.ultra.ui.screen.modulerepo.ModuleRepoDetailScreen
import com.sukisu.ultra.ui.screen.modulerepo.ModuleRepoScreen
import com.sukisu.ultra.ui.screen.settings.SettingPager
import com.sukisu.ultra.ui.screen.settings.SettingsBehaviorScreen
import com.sukisu.ultra.ui.screen.settings.SettingsFeaturesScreen
import com.sukisu.ultra.ui.screen.settings.SettingsGeneralScreen
import com.sukisu.ultra.ui.screen.settings.SettingsModuleScreen
import com.sukisu.ultra.ui.screen.settings.SettingsMoreScreen
import com.sukisu.ultra.ui.screen.settings.tools.ToolsScreen
import com.sukisu.ultra.ui.screen.sulog.SulogScreen
import com.sukisu.ultra.ui.screen.superuser.SuperUserPager
import com.sukisu.ultra.ui.screen.susfs.SuSFSScreen
import com.sukisu.ultra.ui.screen.template.AppProfileTemplateScreen
import com.sukisu.ultra.ui.screen.templateeditor.TemplateEditorScreen
import com.sukisu.ultra.ui.screen.umountmanager.UmountManagerScreen
import com.sukisu.ultra.ui.theme.KernelSUTheme
import com.sukisu.ultra.ui.theme.LocalColorMode
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundOpacity
import com.sukisu.ultra.ui.util.install
import com.sukisu.ultra.ui.util.rememberContentReady
import com.sukisu.ultra.ui.util.rootAvailable
import com.sukisu.ultra.ui.viewmodel.MainActivityViewModel
import com.sukisu.ultra.ui.viewmodel.MainPagerConfig

class MainActivity : ComponentActivity() {
    private val intentChannel = Channel<Intent>(capacity = Channel.BUFFERED)

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isManager = Natives.isManager
        if (isManager && !Natives.requireNewKernel()) install()

        if (savedInstanceState == null) intent?.let { intentChannel.trySend(it) }

        setContent {
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val selectedMainPage by viewModel.selectedMainPage.collectAsStateWithLifecycle()
            val appSettings = uiState.appSettings
            val darkMode = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && isSystemInDarkTheme())

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                )
                window.isNavigationBarContrastEnforced = false
                onDispose { }
            }

            val navigator = rememberNavigator(Route.Main)
            val systemDensity = LocalDensity.current
            val density = remember(systemDensity, uiState.pageScale) {
                Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
            }

            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalDensity provides density,
                LocalColorMode provides appSettings.colorMode.value,
                LocalCustomBackgroundEnabled provides uiState.customBackgroundEnabled,
                LocalCustomBackgroundOpacity provides uiState.customBackgroundOpacity,
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        KernelSUTheme(appSettings = appSettings) {
                        IntentDispatcher(intentChannel = intentChannel)
                        HandleZipFileIntent()
                        val mainScreenEntry = @Composable {
                            MainScreen(
                                initialPage = selectedMainPage,
                                onPageChanged = viewModel::setSelectedMainPage,
                            )
                        }
                        val navDisplay = @Composable {
                            NavDisplay(
                                backStack = navigator.backStack,
                                entryDecorators = listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator()
                                ),
                                onBack = {
                                    when (val top = navigator.current()) {
                                        is Route.TemplateEditor -> {
                                            if (!top.readOnly) {
                                                navigator.setResult("template_edit", true)
                                            } else {
                                                navigator.pop()
                                            }
                                        }

                                        else -> navigator.pop()
                                    }
                                },
                                entryProvider = entryProvider {
                                    entry<Route.Main> { mainScreenEntry() }
                                    entry<Route.About> { AboutScreen() }
                                    entry<Route.Sulog> { SulogScreen() }
                                    entry<Route.ColorPalette> { ColorPaletteScreen() }
                                    entry<Route.SettingsGeneral> { SettingsGeneralScreen() }
                                    entry<Route.SettingsFeatures> { SettingsFeaturesScreen() }
                                    entry<Route.SettingsBehavior> { SettingsBehaviorScreen() }
                                    entry<Route.SettingsModule> { SettingsModuleScreen() }
                                    entry<Route.SettingsMore> { SettingsMoreScreen() }
                                    entry<Route.AppProfileTemplate> { AppProfileTemplateScreen() }
                                    entry<Route.TemplateEditor> { key -> TemplateEditorScreen(key.template, key.readOnly) }
                                    entry<Route.AppProfile> { key -> AppProfileScreen(key.uid) }
                                    entry<Route.ModuleRepo> { ModuleRepoScreen() }
                                    entry<Route.ModuleRepoDetail> { key -> ModuleRepoDetailScreen(key.module) }
                                    entry<Route.Install> { key -> InstallScreen(preselectedKernelUri = key.preselectedKernelUri) }
                                    entry<Route.Flash> { key -> FlashScreen(key.flashIt) }
                                    entry<Route.ExecuteModuleAction> { key -> ExecuteModuleActionScreen(key.moduleId, key.fromShortcut) }
                                    entry<Route.Home> { mainScreenEntry() }
                                    entry<Route.SuperUser> { mainScreenEntry() }
                                    entry<Route.Module> { mainScreenEntry() }
                                    entry<Route.Settings> { mainScreenEntry() }
                                    entry<Route.KernelFlash> { key -> KernelFlashScreen(key.kernelUri, key.selectedSlot, key.kpmPatchEnabled, key.kpmUndoPatch) }
                                    entry<Route.Kpm> { KpmScreen() }
                                    entry<Route.SuSFS> { SuSFSScreen() }
                                    entry<Route.Tool> { ToolsScreen() }
                                    entry<Route.UmountManager> { UmountManagerScreen() }
                                }
                            )
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = if (uiState.customBackgroundEnabled) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ) {
                            BackgroundPage(
                                enabled = uiState.customBackgroundEnabled,
                                uri = uiState.customBackgroundUri,
                                opacity = uiState.customBackgroundOpacity,
                                blurRadius = uiState.customBackgroundBlur,
                                dimAmount = uiState.customBackgroundDim,
                            ) {
                                navDisplay()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentChannel.trySend(intent)
    }
}

@Composable
private fun BackgroundPage(
    enabled: Boolean,
    uri: String?,
    opacity: Float,
    blurRadius: Float,
    dimAmount: Float,
    content: @Composable () -> Unit,
) {
    if (!enabled || uri == null) {
        content()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CustomBackground(
            uriString = uri,
            opacity = opacity,
            blurRadius = blurRadius,
            dimAmount = dimAmount,
        )
        content()
    }
}

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
) {
    val navController = LocalNavigator.current
    val customBackgroundEnabled = LocalCustomBackgroundEnabled.current
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { MainPagerConfig.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)
    val isManager = Natives.isManager
    val isFullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    var userScrollEnabled by remember(isFullFeatured) { mutableStateOf(isFullFeatured) }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val pageContainerColor = if (customBackgroundEnabled) Color.Transparent else surfaceColor

    // FolkPatch nav_mode prefs
    val prefs = remember {
        com.sukisu.ultra.ksuApp.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
    }
    var navMode by remember {
        mutableStateOf(com.sukisu.ultra.ui.navigation.NavMode.fromValue(prefs.getString("nav_mode", "floating")))
    }
    var floatingAutoHide by remember { mutableStateOf(prefs.getBoolean("floating_auto_hide", true)) }
    var floatingSwipeHide by remember { mutableStateOf(prefs.getBoolean("floating_swipe_hide", true)) }
    // Prefs listener + UiRefresh (same idea as FolkPatch refreshTheme for nav)
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    val uiRefreshToken by com.sukisu.ultra.ui.theme.UiRefresh.token.collectAsStateWithLifecycle()
    LaunchedEffect(uiRefreshToken) {
        if (uiRefreshToken > 0L) {
            navMode = com.sukisu.ultra.ui.navigation.NavMode.fromValue(prefs.getString("nav_mode", "floating"))
            floatingAutoHide = prefs.getBoolean("floating_auto_hide", true)
            floatingSwipeHide = prefs.getBoolean("floating_swipe_hide", true)
        }
    }
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            val apply = Runnable {
                when (key) {
                    "nav_mode", null -> {
                        navMode = com.sukisu.ultra.ui.navigation.NavMode.fromValue(
                            p.getString("nav_mode", "floating"),
                        )
                    }
                    "floating_auto_hide" -> floatingAutoHide = p.getBoolean("floating_auto_hide", true)
                    "floating_swipe_hide" -> floatingSwipeHide = p.getBoolean("floating_swipe_hide", true)
                }
            }
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) apply.run()
            else mainHandler.post(apply)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    MainScreenBackHandler(mainPagerState, navController)

    val windowInfo = LocalWindowInfo.current
    val deviceDensity = LocalResources.current.displayMetrics.density
    val widthDp = windowInfo.containerSize.width / deviceDensity
    val heightDp = windowInfo.containerSize.height / deviceDensity
    val autoSplitPane = widthDp >= 840f ||
        (widthDp >= 600f && heightDp / widthDp < 1.2f)

    // FolkPatch: floating | bottom | rail | auto
    val useNavigationRail = when (navMode) {
        com.sukisu.ultra.ui.navigation.NavMode.Rail -> true
        com.sukisu.ultra.ui.navigation.NavMode.Bottom -> false
        com.sukisu.ultra.ui.navigation.NavMode.Floating -> false
        com.sukisu.ultra.ui.navigation.NavMode.Auto -> autoSplitPane
    }
    val isFloatingMode = navMode == com.sukisu.ultra.ui.navigation.NavMode.Floating && !useNavigationRail

    // Floating auto-hide / swipe-hide (FolkPatch MainActivity)
    val isScrollingDown = remember { mutableStateOf(false) }
    val scrollOffset = remember { mutableStateOf(0f) }
    val previousScrollOffset = remember { mutableStateOf(0f) }
    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }
    var autoHideKey by remember { mutableStateOf(0) }
    fun resetBottomBarAutoHide() {
        isBottomBarVisible = true
        autoHideKey++
    }
    LaunchedEffect(isFloatingMode, autoHideKey, floatingAutoHide) {
        if (isFloatingMode && floatingAutoHide && isBottomBarVisible) {
            kotlinx.coroutines.delay(3000L)
            isBottomBarVisible = false
        }
    }
    val showBottomBar = if (isFloatingMode) {
        when {
            !floatingAutoHide && !floatingSwipeHide -> true
            !floatingAutoHide -> !isScrollingDown.value
            !floatingSwipeHide -> isBottomBarVisible
            else -> isBottomBarVisible && !isScrollingDown.value
        }
    } else {
        true
    }
    val bottomBarVisibleState = remember { mutableStateOf(showBottomBar) }
    bottomBarVisibleState.value = showBottomBar

    val scrollConnection = com.sukisu.ultra.ui.navigation.rememberNavScrollConnection(
        isScrollingDown = isScrollingDown,
        scrollOffset = scrollOffset,
        previousScrollOffset = previousScrollOffset,
        onUserScroll = { resetBottomBarAutoHide() },
    )

    CompositionLocalProvider(
        LocalMainPagerState provides mainPagerState,
        com.sukisu.ultra.ui.navigation.LocalBottomBarVisible provides bottomBarVisibleState,
        com.sukisu.ultra.ui.navigation.LocalIsFloatingNavMode provides isFloatingMode,
        com.sukisu.ultra.ui.navigation.LocalNavScrollState provides if (isFloatingMode) {
            com.sukisu.ultra.ui.navigation.NavScrollState(
                isScrollingDown = isScrollingDown,
                scrollOffset = scrollOffset,
                previousScrollOffset = previousScrollOffset,
            )
        } else null,
    ) {
        val contentReady = rememberContentReady()
        val pagerContent = @Composable { bottomInnerPadding: Dp ->
            Box {
                HorizontalPager(
                    state = mainPagerState.pagerState,
                    beyondViewportPageCount = if (contentReady) 3 else 0,
                    userScrollEnabled = userScrollEnabled,
                ) { page ->
                    val isCurrentPage = page == settledPage
                    when (page) {
                        0 -> if (isCurrentPage || contentReady) HomePager(navController, bottomInnerPadding, isCurrentPage)
                        1 -> if (isCurrentPage || contentReady) SuperUserPager(navController, bottomInnerPadding, isCurrentPage)
                        2 -> if (isCurrentPage || contentReady) ModulePager(bottomInnerPadding, isCurrentPage)
                        3 -> if (isCurrentPage || contentReady) SettingPager(navController, bottomInnerPadding)
                    }
                }
            }
        }

        if (useNavigationRail) {
            val startInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Start)
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

            Scaffold(containerColor = pageContainerColor) { _ ->
                Row {
                    SideRail()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .consumeWindowInsets(startInsets)
                    ) {
                        pagerContent(navBarBottomPadding)
                    }
                }
            }
        } else if (isFloatingMode) {
            // FolkPatch floating: bar overlays content; nestedScroll drives hide/show.
            // Home uses HomeBottomSpacer (80.dp + nav); other tabs get floatingPad.
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollConnection)
                ) {
                    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    // FolkPatch HomeBottomSpacer uses 80.dp; match for list pages
                    val floatingPad = if (showBottomBar) 80.dp + navBarBottom else navBarBottom
                    pagerContent(floatingPad)
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBottomBar,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut(),
                ) {
                    BottomBar(
                        isFloating = true,
                        onUserInteraction = { resetBottomBarAutoHide() },
                    )
                }
            }
        } else {
            // FolkPatch fixed bottom bar: content padded 80.dp, bar overlays bottom
            // (MainActivity baseContentModifier.padding(bottom = 80.dp) + BottomBar isFloating=false)
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    pagerContent(0.dp)
                }
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    BottomBar(
                        isFloating = false,
                        onUserInteraction = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navController.current() is Route.Main && navController.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        }
    )
}
