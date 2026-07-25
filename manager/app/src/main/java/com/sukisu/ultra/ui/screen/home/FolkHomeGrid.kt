package com.sukisu.ultra.ui.screen.home

/**
 * GridUI home content (FolkPatch HomeV2 "kernelsu" base):
 * left big status card + right stacked small stat cards (superuser / module),
 * then grid info card and learn-more card.
 */

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundOpacity

@Composable
internal fun FolkHomeScreenGrid(
    state: HomeUiState,
    actions: HomeActions,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top: left big status card + right two small stat cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCardGridBig(
                state = state,
                actions = actions,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmallStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.superuser),
                    value = state.superuserCount.toString(),
                    icon = Icons.Outlined.Security,
                    onClick = actions.onSuperuserClick,
                )
                SmallStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.module),
                    value = state.moduleCount.toString(),
                    icon = Icons.Outlined.Extension,
                    onClick = actions.onModuleClick,
                )
            }
        }

        GridInfoCard(state = state)
        LearnMoreCard(onOpenUrl = actions.onOpenUrl)
    }
}

/** FolkPatch HomeV2 StatusCardBig — 完全一致（含透明度处理），仅状态源换成 MakoSU。 */
@Composable
private fun StatusCardGridBig(
    state: HomeUiState,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val working = isWorking(state)
    val customBgEnabled = LocalCustomBackgroundEnabled.current
    val customBgOpacity = LocalCustomBackgroundOpacity.current
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) { GridWorkingCardBg.load(context) }
    val bgRevision = GridWorkingCardBg.revision
    val useCustomGridBg = GridWorkingCardBg.isSet && GridWorkingCardBg.file(context).exists()

    // Colors (FolkPatch StatusCardBig)
    val (baseContainerColor, baseContentColor) = if (customBgEnabled) {
        val container = MaterialTheme.colorScheme.primary.copy(alpha = customBgOpacity)
        val content = if (customBgOpacity <= 0.1f) {
            if (isDark) Color.White else Color.Black
        } else {
            MaterialTheme.colorScheme.onPrimary
        }
        container to content
    } else {
        if (working) {
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        } else {
            // Unknown/Not Installed → secondaryContainer (FolkPatch)
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
    }

    val containerColor = if (useCustomGridBg) Color.Transparent else baseContainerColor
    // image background + dim → white text (FolkPatch)
    val contentColor = if (useCustomGridBg) Color.White else baseContentColor
    val badge = modeBadge(state)

    var showImageDialog by remember { mutableStateOf(false) }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) GridWorkingCardBg.saveImage(context, uri)
    }

    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text(stringResource(R.string.grid_card_image_title)) },
            confirmButton = {
                TextButton(onClick = {
                    showImageDialog = false
                    pickImage.launch("image/*")
                }) {
                    Text(stringResource(R.string.grid_card_image_change))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    GridWorkingCardBg.clear(context)
                    showImageDialog = false
                }) {
                    Text(stringResource(R.string.grid_card_image_clear))
                }
            },
        )
    }

    FolkTonalCard(modifier = modifier, containerColor = containerColor) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (useCustomGridBg) {
                // GIF support (FolkPatch ImageDecoder/GifDecoder)
                val imageLoader = remember(context) {
                    ImageLoader.Builder(context)
                        .components {
                            if (Build.VERSION.SDK_INT >= 28) {
                                add(ImageDecoderDecoder.Factory())
                            } else {
                                add(GifDecoder.Factory())
                            }
                        }
                        .build()
                }

                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(GridWorkingCardBg.file(context))
                            .memoryCacheKey("grid_working_card_bg_$bgRevision")
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentScale = ContentScale.Crop,
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(GridWorkingCardBg.getEffectiveOpacity(isDark)),
                )
                // Dim layer (FolkPatch gridWorkingCardBackgroundDim)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = GridWorkingCardBg.dim)),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = { if (!state.isLateLoadMode) actions.onInstallClick() },
                        onLongClick = {
                            if (GridWorkingCardBg.isSet) {
                                showImageDialog = true
                            } else {
                                pickImage.launch("image/*")
                            }
                        },
                    )
                    .padding(16.dp),
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = if (working) {
                            stringResource(R.string.home_working)
                        } else if (isSupported(state)) {
                            stringResource(R.string.home_not_installed)
                        } else {
                            stringResource(R.string.home_unsupported)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                    if (working && badge != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "<$badge>",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            Icon(
                imageVector = if (working) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp),
                tint = contentColor,
            )
        }
    }
}

/** FolkPatch HomeV2 SmallInfoCard. */
@Composable
private fun SmallStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    FolkTonalCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
