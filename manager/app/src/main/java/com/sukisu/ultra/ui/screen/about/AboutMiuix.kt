package com.sukisu.ultra.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukisu.ultra.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun AboutScreenMiuix(
    state: AboutUiState,
    actions: AboutScreenActions,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = state.title,
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        val layoutDirection = LocalLayoutDirection.current
                        Icon(
                            modifier = Modifier.graphicsLayer {
                                if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                            },
                            imageVector = MiuixIcons.Back,
                            contentDescription = null,
                            tint = colorScheme.onBackground
                        )
                    }
                },
            )
        },
        popupHost = { },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        ) {
            item(key = "identity") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.mipmap.makosu_launcher_primary),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                        )
                    }
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = state.appName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                    )
                    Text(
                        text = state.versionName,
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = state.copyright,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            item(key = "links") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        colorScheme.surfaceContainer,
                        Color.Transparent,
                    ),
                ) {
                    state.links.forEach { link ->
                        ArrowPreference(
                            title = link.fullText,
                            onClick = { actions.onOpenLink(link.url) },
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.height(
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                )
            }
        }
    }
}
