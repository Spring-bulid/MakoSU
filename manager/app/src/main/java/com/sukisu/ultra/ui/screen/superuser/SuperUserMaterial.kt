package com.sukisu.ultra.ui.screen.superuser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.data.model.AppInfo
import com.sukisu.ultra.ui.component.AppIconImage
import com.sukisu.ultra.ui.component.ScrollToTopOnChange
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.ExpressiveSwitch
import com.sukisu.ultra.ui.component.material.SearchAppBar
import com.sukisu.ultra.ui.component.material.SegmentedColumn
import com.sukisu.ultra.ui.component.material.SegmentedItem
import com.sukisu.ultra.ui.component.material.SegmentedListItem
import com.sukisu.ultra.ui.component.statustag.StatusTag
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled
import com.sukisu.ultra.ui.viewmodel.AppSortConfig
import com.sukisu.ultra.ui.viewmodel.AppSortType

/** One row of the flat (FolkPatch-style) app list: a single app of a uid group. */
private data class FlatApp(
    val group: GroupedApps,
    val app: AppInfo,
    val matched: Boolean = false,
)

private fun List<GroupedApps>.flatten(): List<FlatApp> = flatMap { group ->
    group.apps.map { app ->
        FlatApp(
            group = group,
            app = app,
            matched = group.matchedPackageNames.contains(app.packageName),
        )
    }
}

@Composable
fun SuperUserPagerMaterial(
    uiState: SuperUserUiState,
    actions: SuperUserActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val refreshTick = remember { mutableIntStateOf(0) }
    val pullToRefreshState = rememberPullToRefreshState()

    var localSearchText by remember { mutableStateOf(uiState.searchStatus.searchText) }
    LaunchedEffect(uiState.searchStatus.searchText) {
        localSearchText = uiState.searchStatus.searchText
    }

    val haptic = LocalHapticFeedback.current
    var showOptionsSheet by remember { mutableStateOf(false) }

    val flatApps = remember(uiState.groupedApps) { uiState.groupedApps.flatten() }
    val flatSearchResults = remember(uiState.searchResults) { uiState.searchResults.flatten() }

    if (showOptionsSheet) {
        SuperUserOptionsSheet(
            uiState = uiState,
            onDismiss = { showOptionsSheet = false },
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                actions.onRefresh()
                refreshTick.intValue++
            },
            onUpdateSortConfig = actions.onUpdateSortConfig,
            onToggleShowSystemApps = actions.onToggleShowSystemApps,
            onToggleShowOnlyPrimaryUserApps = actions.onToggleShowOnlyPrimaryUserApps,
        )
    }

    ExpressiveScaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.superuser)) },
                searchText = localSearchText,
                onSearchTextChange = {
                    localSearchText = it
                    actions.onSearchTextChange(it)
                },
                onClearClick = {
                    localSearchText = ""
                    actions.onClearSearch()
                },
                navigationIcon = {
                    IconButton(onClick = actions.onOpenSulog) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Article,
                            contentDescription = stringResource(R.string.settings_sulog)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showOptionsSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(id = R.string.settings)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                actions.onRefresh()
                refreshTick.intValue++
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = uiState.isRefreshing,
                    state = pullToRefreshState,
                )
            },
        ) {
            val latestGroupedApps = rememberUpdatedState(uiState.groupedApps)
            val latestRefreshing = rememberUpdatedState(uiState.isRefreshing)
            ScrollToTopOnChange(
                listState,
                uiState.sortConfig,
                uiState.showSystemApps,
                uiState.showOnlyPrimaryUserApps,
                refreshTick.intValue,
                localSearchText,
                isBusy = { latestRefreshing.value },
            ) { latestGroupedApps.value }

            val displayedApps = if (localSearchText.isBlank()) flatApps else flatSearchResults
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 0.dp,
                    bottom = 16.dp + bottomInnerPadding
                ),
            ) {
                itemsIndexed(
                    displayedApps,
                    key = { _, item -> "${item.group.uid}:${item.app.packageName}" }
                ) { index, item ->
                    SegmentedItem(index = index, count = displayedApps.size) {
                        AppItem(
                            item = item,
                            onClick = { actions.onOpenProfile(item.group) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * FolkPatch-style flat app card (AppItemM3E analog): round app icon, label,
 * package name, status tags below, trailing chevron. Tap opens the profile
 * for the app's uid (same navigation as before).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppItem(
    item: FlatApp,
    onClick: () -> Unit,
) {
    val group = item.group
    val app = item.app

    val bg = colorScheme.primary
    val fg = colorScheme.onPrimary
    val umountBg = colorScheme.tertiaryContainer
    val umountFg = colorScheme.onTertiaryContainer
    val customBg = colorScheme.secondaryContainer
    val customFg = colorScheme.onSecondaryContainer
    val otherBg = colorScheme.tertiary
    val otherFg = colorScheme.onTertiary

    val userId = group.uid / 100000
    val tags = remember(app.allowSu, app.hasCustomProfile, group.shouldUmount, userId) {
        buildList {
            if (app.allowSu) add(StatusMeta("ROOT", bg, fg))
            if (group.shouldUmount) add(StatusMeta("UMOUNT", umountBg, umountFg))
            if (app.hasCustomProfile) add(StatusMeta("CUSTOM", customBg, customFg))
            if (userId != 0) add(StatusMeta("USER $userId", otherBg, otherFg))
        }
    }

    val containerColor = when {
        item.matched -> colorScheme.secondaryContainer
        LocalCustomBackgroundEnabled.current -> colorScheme.surfaceContainer.copy(alpha = 0.72f)
        else -> colorScheme.surfaceContainer
    }

    SegmentedListItem(
        onClick = onClick,
        colors = ListItemDefaults.segmentedColors(
            containerColor = containerColor,
            supportingContentColor = colorScheme.onSurfaceVariant,
        ),
        headlineContent = {
            Text(
                text = app.label,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = app.packageName,
                    color = colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        tags.forEach { tag ->
                            StatusTag(
                                label = tag.label,
                                backgroundColor = tag.bg,
                                contentColor = tag.fg
                            )
                        }
                    }
                }
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        leadingContent = {
            AppIconImage(
                packageInfo = app.packageInfo,
                label = app.label,
                modifier = Modifier.size(48.dp)
            )
        },
    )
}

/**
 * FolkPatch-style options sheet: refresh, filters and sort options in a
 * ModalBottomSheet instead of toolbar dropdown menus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuperUserOptionsSheet(
    uiState: SuperUserUiState,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateSortConfig: (AppSortConfig) -> Unit,
    onToggleShowSystemApps: () -> Unit,
    onToggleShowOnlyPrimaryUserApps: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sortConfig = uiState.sortConfig

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.superuser),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            SheetItem(
                icon = Icons.Filled.Refresh,
                title = stringResource(R.string.refresh),
                onClick = {
                    onRefresh()
                    onDismiss()
                },
            )

            SheetSectionTitle(stringResource(R.string.superuser_filter))
            SheetSwitchItem(
                icon = Icons.Filled.Visibility,
                title = stringResource(R.string.show_system_apps),
                checked = uiState.showSystemApps,
                onToggle = onToggleShowSystemApps,
            )
            if (uiState.userIds.size > 1) {
                SheetSwitchItem(
                    icon = Icons.Filled.Person,
                    title = stringResource(R.string.show_only_primary_user_apps),
                    checked = uiState.showOnlyPrimaryUserApps,
                    onToggle = onToggleShowOnlyPrimaryUserApps,
                )
            }

            SheetSectionTitle(stringResource(R.string.menu_sort))
            val sortEntries = listOf(
                AppSortType.NAME to R.string.sort_by_name,
                AppSortType.PACKAGE_NAME to R.string.sort_by_package_name,
                AppSortType.INSTALL_TIME to R.string.sort_by_install_time,
                AppSortType.UPDATE_TIME to R.string.sort_by_update_time,
            )
            sortEntries.forEach { (type, resId) ->
                SheetItem(
                    title = stringResource(resId),
                    onClick = { onUpdateSortConfig(sortConfig.withType(type)) },
                    trailing = {
                        if (sortConfig.sortType == type) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                )
            }
            SheetSwitchItem(
                title = stringResource(R.string.sort_reverse),
                checked = sortConfig.reversed,
                onToggle = { onUpdateSortConfig(sortConfig.toggleReversed()) },
            )
        }
    }
}

@Composable
private fun SheetSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun SheetItem(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
    }
}

@Composable
private fun SheetSwitchItem(
    title: String,
    checked: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector? = null,
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    role = Role.Switch,
                    onValueChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onToggle()
                    },
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            ExpressiveSwitch(
                checked = checked,
                onCheckedChange = null,
            )
        }
    }
}
