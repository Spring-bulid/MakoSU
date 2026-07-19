package com.sukisu.ultra.ui.component.material

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Compact search app bar following FolkPatch's interaction model: the normal
 * toolbar stays clean and search only expands after the search action is used.
 */
@Composable
fun SearchAppBar(
    title: @Composable () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClearClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    defaultContent: @Composable BoxScope.(bottomPadding: Dp, closeSearch: () -> Unit) -> Unit = { _, _ -> },
    searchContent: @Composable BoxScope.(bottomPadding: Dp, closeSearch: () -> Unit) -> Unit = { _, _ -> }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val searchBarState = rememberContainedSearchBarState()
    var query by rememberSaveable { mutableStateOf(searchText) }

    val isSearchExpanded =
        searchBarState.currentValue != SearchBarValue.Collapsed ||
            searchBarState.targetValue != SearchBarValue.Collapsed

    val clearQuery: () -> Unit = {
        query = ""
        onClearClick()
    }
    val closeSearch: () -> Unit = {
        clearQuery()
        focusManager.clearFocus()
        keyboardController?.hide()
        scope.launch { searchBarState.animateToCollapsed() }
    }

    LaunchedEffect(searchText) {
        if (query != searchText) query = searchText
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) focusRequester.requestFocus()
    }

    DisposableEffect(Unit) {
        onDispose { keyboardController?.hide() }
    }

    BackHandler(enabled = isSearchExpanded) { closeSearch() }

    TopAppBar(
        title = title,
        navigationIcon = { navigationIcon?.invoke() },
        actions = {
            IconButton(
                onClick = {
                    scope.launch { searchBarState.animateToExpanded() }
                }
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
            }
            actions?.invoke()
        },
        colors = expressiveTopAppBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        scrollBehavior = scrollBehavior,
    )

    ExpandedFullScreenContainedSearchBar(
        state = searchBarState,
        inputField = {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onSearchTextChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp, end = 14.dp)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(15.dp),
                leadingIcon = {
                    IconButton(onClick = closeSearch) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = clearQuery) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
            )
        },
        windowInsets = {
            SearchBarDefaults.fullScreenWindowInsets.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            )
        },
        content = {
            val bottomPadding = SearchBarDefaults.fullScreenWindowInsets
                .asPaddingValues()
                .calculateBottomPadding()
            Box(modifier = Modifier.fillMaxSize()) {
                if (query.isEmpty()) {
                    defaultContent(bottomPadding, closeSearch)
                } else {
                    searchContent(bottomPadding, closeSearch)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(bottom = 16.dp)
                ) {
                    SnackBarHost(hostState = snackbarHostState)
                }
            }
        },
    )
}
