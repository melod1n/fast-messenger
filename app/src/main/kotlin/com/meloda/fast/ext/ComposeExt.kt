package com.meloda.fast.ext

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.util.AndroidUtils

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

@Composable
fun UiText?.getString(): String? {
    return when (this) {
        is UiText.Resource -> {
            stringResource(id = resId)
        }

        is UiText.ResourceParams -> {
            // TODO: 26/11/2023, Danil Nikolaev: fix
            val processedArgs = args.map { any ->
                when (any) {
                    is UiText -> any.getString()
                    else -> any
                }
            }.toTypedArray()

            stringResource(id = value, processedArgs)
        }

        is UiText.QuantityResource -> {
            pluralStringResource(id = resId, count = quantity, quantity)
        }

        is UiText.Simple -> {
            text
        }

        else -> null
    }
}

@Composable
fun isUsingDarkThemeComposable(): Boolean {
    if (LocalView.current.isInEditMode) {
        return false
    }

    return isUsingDarkTheme()
}

fun isUsingDarkTheme(): Boolean {
    val nightThemeMode = AppGlobal.preferences.getInt(
        SettingsKeys.KEY_APPEARANCE_DARK_THEME,
        SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
    )
    val appForceDarkMode = nightThemeMode == AppCompatDelegate.MODE_NIGHT_YES
    val appBatterySaver = nightThemeMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

    val systemUiNightMode = AppGlobal.resources.configuration.uiMode

    val isSystemBatterySaver = AndroidUtils.isBatterySaverOn()
    val isSystemUsingDarkTheme =
        systemUiNightMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    return appForceDarkMode || (appBatterySaver && isSystemBatterySaver) || (!appBatterySaver && isSystemUsingDarkTheme && nightThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

@Composable
fun isUsingDynamicColorsComposable(): Boolean =
    if (LocalView.current.isInEditMode) true
    else {
        isUsingDynamicColors()
    }

fun isUsingDynamicColors(): Boolean = AppGlobal.preferences.getBoolean(
    SettingsKeys.KEY_USE_DYNAMIC_COLORS,
    SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
)

@Composable
fun LocalContentAlpha(
    defaultColor: Color = MaterialTheme.colorScheme.onBackground,
    alpha: Float,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides defaultColor.copy(alpha = alpha)
    ) {
        content()
    }
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
