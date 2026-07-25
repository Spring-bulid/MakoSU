package com.sukisu.ultra.ui.component.material

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * FolkPatch inline search app bar: the search field replaces the title inside
 * the top bar (fade in/out); the content below filters live. No fullscreen
 * search page.
 */
@Composable
fun SearchAppBar(
    title: @Composable () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClearClick: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var onSearch by rememberSaveable { mutableStateOf(false) }

    if (onSearch) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    BackHandler(
        enabled = onSearch,
        onBack = {
            keyboardController?.hide()
            onClearClick()
            onSearch = false
        }
    )

    DisposableEffect(Unit) {
        onDispose { keyboardController?.hide() }
    }

    TopAppBar(
        title = {
            Box {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterStart),
                    visible = !onSearch,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    content = { title() }
                )

                AnimatedVisibility(
                    visible = onSearch,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 2.dp, end = 14.dp)
                            .focusRequester(focusRequester),
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        shape = RoundedCornerShape(15.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onSearch = false
                                    keyboardController?.hide()
                                    onClearClick()
                                },
                                content = { Icon(Icons.Filled.Close, null) }
                            )
                        },
                        maxLines = 1,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions {
                            defaultKeyboardAction(ImeAction.Search)
                            keyboardController?.hide()
                        },
                    )
                }
            }
        },
        navigationIcon = {
            if (onSearch) {
                IconButton(
                    onClick = {
                        onSearch = false
                        keyboardController?.hide()
                        onClearClick()
                    },
                    content = { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) }
                )
            } else {
                navigationIcon?.invoke()
            }
        },
        actions = {
            AnimatedVisibility(visible = !onSearch) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onSearch = true },
                        content = { Icon(Icons.Filled.Search, null) }
                    )
                    actions?.invoke()
                }
            }
        },
        colors = expressiveTopAppBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        scrollBehavior = scrollBehavior,
    )
}
