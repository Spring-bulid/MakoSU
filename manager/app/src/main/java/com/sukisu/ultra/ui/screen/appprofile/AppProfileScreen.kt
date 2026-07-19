package com.sukisu.ultra.ui.screen.appprofile

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.sukisu.ultra.Natives
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.navigation3.Route
import com.sukisu.ultra.ui.util.forceStopApp
import com.sukisu.ultra.ui.util.getSepolicy
import com.sukisu.ultra.ui.util.launchApp
import com.sukisu.ultra.ui.util.restartApp
import com.sukisu.ultra.ui.util.setSepolicy
import com.sukisu.ultra.ui.viewmodel.SuperUserViewModel
import com.sukisu.ultra.ui.viewmodel.getTemplateInfoById

@Composable
fun AppProfileScreen(uid: Int) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHost = remember { SnackbarHostState() }
    val viewModel: SuperUserViewModel = viewModel()
    val appGroupState = remember(uid) {
        derivedStateOf {
            viewModel.uiState.value.groupedApps.find { it.uid == uid } ?: SuperUserViewModel.getGroupedApp(uid)
        }
    }
    val appGroup = appGroupState.value
    val primaryAppInfo = appGroup?.primary
    if (primaryAppInfo == null) {
        LaunchedEffect(Unit) {
            navigator.pop()
        }
        return
    }

    val packageName = primaryAppInfo.packageName
    val sharedUserId = remember(uid) {
        primaryAppInfo.packageInfo.sharedUserId
            ?: appGroup.apps.firstOrNull { it.packageInfo.sharedUserId != null }?.packageInfo?.sharedUserId
            ?: ""
    }

    val initialProfile = remember(uid, packageName) {
        Natives.getAppProfile(packageName, uid).also {
            if (it.allowSu) {
                it.rules = getSepolicy(packageName)
            }
        }
    }
    var profile by rememberSaveable(uid, packageName) {
        mutableStateOf(initialProfile)
    }

    val failToUpdateAppProfile = stringResource(R.string.failed_to_update_app_profile).format(primaryAppInfo.label)
    val failToUpdateSepolicy = stringResource(R.string.failed_to_update_sepolicy).format(primaryAppInfo.label)
    val suNotAllowed = stringResource(R.string.su_not_allowed).format(primaryAppInfo.label)

    fun showMessage(message: String) {
        scope.launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val state = AppProfileUiState(
        uid = uid,
        packageName = packageName,
        profile = profile,
        appGroup = appGroup,
        sharedUserId = sharedUserId,
    )

    val actions = AppProfileActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onLaunchApp = ::launchApp,
        onForceStopApp = ::forceStopApp,
        onRestartApp = ::restartApp,
        onViewTemplate = { templateId ->
            getTemplateInfoById(templateId)?.let { info ->
                navigator.push(Route.TemplateEditor(info, true))
            }
        },
        onManageTemplate = {
            navigator.push(Route.AppProfileTemplate)
        },
        onProfileChange = { updatedProfile ->
            scope.launch {
                if (updatedProfile.allowSu) {
                    if (uid < 2000 && uid != 1000) {
                        showMessage(suNotAllowed)
                        return@launch
                    }
                    if (!updatedProfile.rootUseDefault
                        && updatedProfile.rules.isNotEmpty()
                        && !setSepolicy(profile.name, updatedProfile.rules)
                    ) {
                        showMessage(failToUpdateSepolicy)
                        return@launch
                    }
                }
                if (!Natives.setAppProfile(updatedProfile)) {
                    showMessage(failToUpdateAppProfile)
                } else {
                    profile = updatedProfile
                }
            }
        },
    )

    AppProfileScreenMaterial(
            state = state,
            actions = actions,
            snackBarHost = snackBarHost,
        )
}
