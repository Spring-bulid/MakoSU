package com.sukisu.ultra.ui.screen.settings

import android.app.Activity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.material.SegmentedColumn
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.theme.AppLanguage

/**
 * FolkPatch LanguagePickerScreen equivalent: follow-system + all shipped locales,
 * applied immediately via Activity.recreate().
 */
@Composable
fun LanguagePickerScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val activity = context as? Activity
    var current by remember { mutableStateOf(AppLanguage.currentLanguageTag(context)) }

    fun select(tag: String?) {
        if (tag == current) return
        AppLanguage.setLanguage(context, tag)
        current = tag
        activity?.recreate()
    }

    SettingsCategoryScaffold(
        title = stringResource(R.string.language_settings_app_language),
        onBack = dropUnlessResumed { navigator.pop() },
    ) {
        SegmentedColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            item(key = "system") {
                LanguageRow(
                    icon = Icons.Filled.Translate,
                    name = stringResource(R.string.language_follow_system),
                    selected = current == null,
                    onClick = { select(null) },
                )
            }
            AppLanguage.SUPPORTED_LANGUAGES.forEach { tag ->
                item(key = tag) {
                    LanguageRow(
                        name = AppLanguage.nativeName(tag),
                        selected = current == tag,
                        onClick = { select(tag) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
        }
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        RadioButton(selected = selected, onClick = null)
    }
}
