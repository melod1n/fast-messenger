package com.meloda.app.fast.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.meloda.app.fast.datastore.isUsingAmoledBackground
import com.meloda.app.fast.datastore.isUsingDynamicColors
import com.meloda.app.fast.datastore.model.ThemeConfig
import com.meloda.app.fast.datastore.selectedColorScheme
import com.meloda.app.fast.designsystem.colorschemes.ClassicColorScheme
import dev.chrisbanes.haze.HazeState

private val googleSansFonts = FontFamily(
    Font(resId = R.font.google_sans_regular),
    Font(
        resId = R.font.google_sans_italic,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.google_sans_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.google_sans_medium_italic,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.google_sans_bold,
        weight = FontWeight.Bold
    ),
    Font(
        resId = R.font.google_sans_bold_italic,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    )
)

private val robotoFonts = FontFamily(
    Font(
        resId = R.font.roboto_thin,
        weight = FontWeight.Thin
    ),
    Font(
        resId = R.font.roboto_thin_italic,
        weight = FontWeight.Thin,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.roboto_light,
        weight = FontWeight.Light
    ),
    Font(
        resId = R.font.roboto_light_italic,
        weight = FontWeight.Light,
        style = FontStyle.Italic
    ),
    Font(resId = R.font.roboto_regular),
    Font(
        resId = R.font.roboto_italic,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.roboto_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.roboto_medium_italic,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.roboto_bold,
        weight = FontWeight.Bold
    ),
    Font(
        resId = R.font.roboto_bold_italic,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.roboto_black,
        weight = FontWeight.Black
    ),
    Font(
        resId = R.font.roboto_black_italic,
        weight = FontWeight.Black,
        style = FontStyle.Italic
    )
)

val LocalTheme = compositionLocalOf {
    ThemeConfig(
        usingDarkStyle = false,
        usingDynamicColors = false,
        selectedColorScheme = 0,
        usingAmoledBackground = false,
        usingBlur = false,
        multiline = false
    )
}

val LocalHazeState = compositionLocalOf {
    HazeState()
}

val LocalBottomPadding = compositionLocalOf {
    0.dp
}

@Composable
fun AppTheme(
    predefinedColorScheme: ColorScheme? = null,
    useDarkTheme: Boolean = isUsingDarkTheme(),
    useDynamicColors: Boolean = isUsingDynamicColors(),
    selectedColorScheme: Int = selectedColorScheme(),
    useAmoledBackground: Boolean = isUsingAmoledBackground(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when {
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        else -> {
            // TODO: 03/07/2024, Danil Nikolaev: add color picker to settings
            when (selectedColorScheme) {
                1 -> if (useDarkTheme) darkColorScheme() else lightColorScheme()
                else -> if (useDarkTheme) ClassicColorScheme.darkScheme else ClassicColorScheme.lightScheme
            }
        }
    }.let { scheme ->
        if (useDarkTheme && useAmoledBackground) {
            scheme.copy(
                background = Color.Black,
                surface = Color.Black
            )
        } else {
            scheme
        }
    }

    val typography = MaterialTheme.typography.copy(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = googleSansFonts),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = googleSansFonts),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = googleSansFonts),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = googleSansFonts),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = googleSansFonts),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = googleSansFonts),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = robotoFonts),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = robotoFonts),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = robotoFonts)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = predefinedColorScheme ?: colorScheme,
        typography = typography,
        content = content
    )
}
