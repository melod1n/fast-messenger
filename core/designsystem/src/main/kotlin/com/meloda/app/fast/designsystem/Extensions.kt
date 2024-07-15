package com.meloda.app.fast.designsystem

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.util.AndroidUtils
import com.meloda.app.fast.datastore.SettingsController
import com.meloda.app.fast.datastore.SettingsKeys

@Composable
fun isUsingDarkTheme(): Boolean {
    val nightThemeMode = SettingsController.getInt(
        SettingsKeys.KEY_APPEARANCE_DARK_THEME,
        SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
    )
    val appForceDarkMode = nightThemeMode == AppCompatDelegate.MODE_NIGHT_YES
    val appBatterySaver = nightThemeMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

    val context = LocalContext.current

    val systemUiNightMode = context.resources.configuration.uiMode

    val isSystemBatterySaver = AndroidUtils.isBatterySaverOn(context)
    val isSystemUsingDarkTheme =
        systemUiNightMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    return appForceDarkMode || (appBatterySaver && isSystemBatterySaver) || (!appBatterySaver && isSystemUsingDarkTheme && nightThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

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

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
