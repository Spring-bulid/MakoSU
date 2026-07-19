package com.sukisu.ultra.ui.screen.home

/**
 * Shared pieces cloned from FolkPatch Home.kt / HomeCircle.kt,
 * adapted to MakoSU HomeUiState (KernelSU) instead of KP/AP states.
 */

import android.os.Build
import android.system.Os
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled

internal val FolkCardShape = RoundedCornerShape(20.dp)

/** FolkPatch HomeCircle.TonalCard */
@Composable
internal fun FolkTonalCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    shape: Shape = FolkCardShape,
    content: @Composable () -> Unit,
) {
    val finalContainerColor = containerColor ?: if (LocalCustomBackgroundEnabled.current) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = finalContainerColor),
        shape = shape,
    ) {
        content()
    }
}

/** FolkPatch StatusBadge (List working card) */
@Composable
internal fun StatusBadge(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** FolkPatch ModeLabelText (Circle status) */
@Composable
internal fun ModeLabelText(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    containerColor: Color = MaterialTheme.colorScheme.primary,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .padding(end = 4.dp)
            .background(color = containerColor, shape = RoundedCornerShape(4.dp)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 5.dp),
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
            ),
        )
    }
}

internal fun getSystemVersion(): String {
    return "${Build.VERSION.RELEASE} ${if (Build.VERSION.PREVIEW_SDK_INT != 0) "Preview" else ""} (API ${Build.VERSION.SDK_INT})"
}

internal fun getDeviceInfo(): String {
    var manufacturer =
        Build.MANUFACTURER[0].uppercaseChar().toString() + Build.MANUFACTURER.substring(1)
    if (!Build.BRAND.equals(Build.MANUFACTURER, ignoreCase = true)) {
        manufacturer += " " + Build.BRAND[0].uppercaseChar() + Build.BRAND.substring(1)
    }
    manufacturer += " " + Build.MODEL + " "
    return manufacturer
}

/** Mode badge text: LKM / GKI — maps FolkPatch Full/Half */
@Composable
internal fun modeBadge(state: HomeUiState): String? {
    if (state.ksuVersion == null) return null
    return when (state.lkmMode) {
        true -> "LKM"
        false -> "GKI"
        null -> null
    }
}

@Composable
internal fun isWorking(state: HomeUiState): Boolean = state.ksuVersion != null

@Composable
internal fun isSupported(state: HomeUiState): Boolean = state.kernelVersion.isGKI()

@Composable
internal fun ksuVersionLabel(state: HomeUiState): String {
    val v = state.ksuVersion ?: return "N/A"
    return "$v-${state.kernelUAPIVersion}"
}

@Composable
internal fun selinuxDisplay(status: String): String = when (status) {
    "Enforcing" -> stringResource(R.string.selinux_status_enforcing)
    "Permissive" -> stringResource(R.string.selinux_status_permissive)
    "Disabled" -> stringResource(R.string.selinux_status_disabled)
    else -> stringResource(R.string.selinux_status_unknown)
}

/**
 * FolkPatch ListInfoCard / InfoCard — plain stacked labels (no icons).
 * Home.kt ListInfoCard padding start/top/end 24 bottom 16, bodyLarge + bodyMedium, 16 gaps.
 */
@Composable
internal fun ListInfoCard(state: HomeUiState) {
    val info = state.systemInfo
    FolkTonalCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
        ) {
            @Composable
            fun Item(label: String, content: String) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                Text(text = content, style = MaterialTheme.typography.bodyMedium)
            }

            // FolkPatch ListInfoCard: kpatch version when installed → KernelSU version here
            if (state.ksuVersion != null) {
                Item("KernelSU", ksuVersionLabel(state))
                Spacer(Modifier.height(16.dp))
            }
            Item(stringResource(R.string.home_manager_version), info.managerVersion)
            Spacer(Modifier.height(16.dp))
            Item(stringResource(R.string.home_device_model), getDeviceInfo().trim())
            Spacer(Modifier.height(16.dp))
            Item(stringResource(R.string.home_kernel), Os.uname().release)
            Spacer(Modifier.height(16.dp))
            Item(stringResource(R.string.home_system_version), getSystemVersion())
            if (state.showFullStatus) {
                Spacer(Modifier.height(16.dp))
                Item(stringResource(R.string.home_fingerprint), info.fingerprint)
            }
            Spacer(Modifier.height(16.dp))
            Item(stringResource(R.string.home_selinux_status), selinuxDisplay(info.selinuxStatus))
        }
    }
}

/**
 * FolkPatch InfoCard used by Grid V2 — same plain list as ListInfoCard.
 */
@Composable
internal fun GridInfoCard(state: HomeUiState) = ListInfoCard(state)

/**
 * FolkPatch InfoCardCircle — icon rows, padding h20 v24, 16 gaps.
 */
@Composable
internal fun InfoCardCircle(state: HomeUiState) {
    val info = state.systemInfo
    val susfs = rememberSusfsInfo(stringResource(R.string.manual_hook), stringResource(R.string.inline_hook))
    val hook = rememberHookTypeLabel(
        stringResource(R.string.manual_hook),
        stringResource(R.string.inline_hook),
        stringResource(R.string.tracepoint_hook),
        stringResource(R.string.selinux_status_unknown),
    )
    FolkTonalCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            @Composable
            fun Item(icon: ImageVector, label: String, content: String) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }

            Item(Icons.Outlined.Apps, stringResource(R.string.home_manager_version), info.managerVersion)
            Spacer(Modifier.height(16.dp))
            if (state.ksuVersion != null) {
                Item(Icons.Outlined.Extension, stringResource(R.string.home_working), ksuVersionLabel(state))
                Spacer(Modifier.height(16.dp))
            }
            Item(Icons.Outlined.PhoneAndroid, stringResource(R.string.home_device_model), getDeviceInfo().trim())
            Spacer(Modifier.height(16.dp))
            Item(Icons.Outlined.DeveloperBoard, stringResource(R.string.home_kernel), Os.uname().release)
            Spacer(Modifier.height(16.dp))
            Item(Icons.Outlined.Info, stringResource(R.string.home_system_version), getSystemVersion())
            if (state.showFullStatus) {
                Spacer(Modifier.height(16.dp))
                Item(Icons.Outlined.Fingerprint, stringResource(R.string.home_fingerprint), info.fingerprint)
            }
            if (state.showFullStatus && susfs.status == SusfsStatus.Supported) {
                Spacer(Modifier.height(16.dp))
                Item(Icons.Outlined.Code, stringResource(R.string.home_susfs_version), susfs.detail)
            } else if (state.showFullStatus && !hook.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Item(Icons.Outlined.Code, stringResource(R.string.hook_type), hook)
            }
            if (state.showFullStatus && !info.kernelFullVersion.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Item(Icons.Outlined.Memory, stringResource(R.string.home_kernel_full_version), info.kernelFullVersion)
            }
            Spacer(Modifier.height(16.dp))
            Item(Icons.Outlined.Shield, stringResource(R.string.home_selinux_status), selinuxDisplay(info.selinuxStatus))
        }
    }
}

/** FolkPatch LearnMoreCard / LearnMoreCardCircle */
@Composable
internal fun LearnMoreCard(onOpenUrl: (String) -> Unit) {
    val url = stringResource(R.string.home_learn_kernelsu_url)
    FolkTonalCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenUrl(url) }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_learn_kernelsu),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_click_to_learn_kernelsu),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
