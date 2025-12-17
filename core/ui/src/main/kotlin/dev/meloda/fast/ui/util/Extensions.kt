package dev.meloda.fast.ui.util

import android.content.res.Configuration
import android.os.PowerManager
import android.view.KeyEvent
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toDrawable
import dev.meloda.fast.common.model.DarkMode
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.model.UiText

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

@Composable
fun UiImage.getResourcePainter(): Painter? {
    return when (this) {
        is UiImage.Resource -> painterResource(id = resId)
        else -> null
    }
}

@Composable
fun UiImage.getImage(): Any {
    return when (this) {
        is UiImage.Color -> color.toDrawable()
        is UiImage.ColorResource -> colorResource(id = resId).toArgb().toDrawable()
        is UiImage.Resource -> painterResource(id = resId)
        is UiImage.Simple -> drawable
        is UiImage.Url -> url
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

@Composable
fun isNeedToEnableDarkMode(darkMode: DarkMode): Boolean {
    val context = LocalContext.current

    val appForceDarkMode = darkMode == DarkMode.ENABLED
    val appBatterySaver = darkMode == DarkMode.AUTO_BATTERY

    val systemUiNightMode = context.resources.configuration.uiMode

    val isSystemBatterySaver = context.getSystemService<PowerManager>()?.isPowerSaveMode == true
    val isSystemUsingDarkTheme =
        systemUiNightMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    return appForceDarkMode || (appBatterySaver && isSystemBatterySaver) || (!appBatterySaver && isSystemUsingDarkTheme && darkMode == DarkMode.FOLLOW_SYSTEM)
}

fun Color.lighten(amount: Float) = lerp(this, Color.White, amount.coerceIn(0f, 1f))
fun Color.darken(amount: Float) = lerp(this, Color.Black, amount.coerceIn(0f, 1f))

fun Color.isDark(
    background: Color = Color.White,
    threshold: Float = 0.5f
): Boolean {
    val opaque = if (alpha < 1f) this.compositeOver(background) else this
    return opaque.luminance() < threshold
}
