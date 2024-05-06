package com.meloda.app.fast.designsystem

import android.content.Context
import android.content.res.Configuration
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.extensions.preferences
import com.meloda.app.fast.common.util.AndroidUtils
import com.meloda.app.fast.datastore.SettingsKeys

// TODO: 05/05/2024, Danil Nikolaev: extract to separated module
@Composable
fun isUsingDarkTheme(): Boolean {
    val context = LocalContext.current

    val nightThemeMode = context.preferences.getInt(
        SettingsKeys.KEY_APPEARANCE_DARK_THEME,
        SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
    )
    val appForceDarkMode = nightThemeMode == AppCompatDelegate.MODE_NIGHT_YES
    val appBatterySaver = nightThemeMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

    val systemUiNightMode = context.resources.configuration.uiMode

    val isSystemBatterySaver = AndroidUtils.isBatterySaverOn(context)
    val isSystemUsingDarkTheme =
        systemUiNightMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    return appForceDarkMode || (appBatterySaver && isSystemBatterySaver) || (!appBatterySaver && isSystemUsingDarkTheme && nightThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

@Composable
fun isUsingDynamicColors(): Boolean = LocalContext.current.preferences.getBoolean(
    SettingsKeys.KEY_USE_DYNAMIC_COLORS,
    SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
)

@Composable
fun isUsingAmoledBackground(): Boolean = LocalContext.current.preferences.getBoolean(
    SettingsKeys.KEY_APPEARANCE_AMOLED_THEME,
    SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME
)

@Composable
fun isUsingBlur(): Boolean = LocalContext.current.preferences.getBoolean(
    SettingsKeys.KEY_APPEARANCE_BLUR,
    SettingsKeys.DEFAULT_VALUE_KEY_APPEARANCE_BLUR,
)

@Composable
fun isDebugSettingsShownComposable(): Boolean =
    if (LocalView.current.isInEditMode) true
    else {
        isDebugSettingsShown(LocalContext.current)
    }

fun isDebugSettingsShown(context: Context): Boolean = context.preferences.getBoolean(
    SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
    SettingsKeys.DEFAULT_VALUE_SHOW_DEBUG_CATEGORY
)

@Composable
fun UiText?.getString(): String? {
    return when (this) {
        is UiText.Resource -> {
            stringResource(id = resId)
        }

        is UiText.ResourceParams -> {
            val processedArgs = args.map { any ->
                when (any) {
                    is UiText -> any.getString().orEmpty()
                    else -> any.toString()
                }
            }.toTypedArray()

            stringResource(id = value, *processedArgs)
        }

        is UiText.QuantityResource -> {
            pluralStringResource(id = resId, count = quantity, quantity)
        }

        is UiText.Simple -> text

        else -> null
    }
}

fun Modifier.handleTabKey(
    action: () -> Boolean
): Modifier = this.onKeyEvent { event ->
    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_TAB) {
        action.invoke()
    } else false
}

fun Modifier.handleEnterKey(
    action: () -> Boolean
): Modifier = this.onKeyEvent { event ->
    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
        action.invoke()
    } else false
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPermission(
    showRationale: @Composable () -> Unit,
    onDenied: @Composable () -> Unit,
    permission: PermissionState,
) {
    when (val status = permission.status) {
        is PermissionStatus.Denied -> {
            if (status.shouldShowRationale) {
                showRationale()
            } else {
                onDenied()
            }
        }

        is PermissionStatus.Granted -> Unit
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermission(
    permission: PermissionState
) {
    LaunchedEffect(Unit) { permission.launchPermissionRequest() }
}

