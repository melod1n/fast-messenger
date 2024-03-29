package com.meloda.fast.ext

import android.content.res.Configuration
import android.media.AudioManager
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.parseString
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.AndroidUtils

@ExperimentalFoundationApi
fun Modifier.clickableSound(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: (() -> Unit)? = null
): Modifier = this.clickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = {
        AppGlobal.audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
        onClick?.invoke()
    }
)

@ExperimentalFoundationApi
fun Modifier.combinedClickableSound(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    this.combinedClickableSound(
        interactionSource = remember { MutableInteractionSource() },
        indication = LocalIndication.current,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = {
            AppGlobal.audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
            onClick?.invoke()
        }
    )
}

@ExperimentalFoundationApi
fun Modifier.combinedClickableSound(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
): Modifier = this.combinedClickable(
    interactionSource = interactionSource,
    indication = indication,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onLongClickLabel = onLongClickLabel,
    onLongClick = onLongClick,
    onDoubleClick = onDoubleClick,
    onClick = {
        AppGlobal.audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
        onClick?.invoke()
    }
)

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
    return this.parseString(LocalContext.current)
}

@Composable
fun isUsingDarkTheme(): Boolean {
    if (LocalView.current.isInEditMode) {
        return false
    }

    val nightThemeMode = AppGlobal.preferences.getInt(
        SettingsFragment.KEY_APPEARANCE_DARK_THEME,
        SettingsFragment.DEFAULT_VALUE_APPEARANCE_DARK_THEME
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
fun isUsingDynamicColors(): Boolean =
    if (LocalView.current.isInEditMode) true
    else {
        AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_USE_DYNAMIC_COLORS,
            SettingsFragment.DEFAULT_VALUE_USE_DYNAMIC_COLORS
        )
    }
