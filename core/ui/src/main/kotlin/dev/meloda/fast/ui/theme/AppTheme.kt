package dev.meloda.fast.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import dev.chrisbanes.haze.HazeState
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.DeviceSize
import dev.meloda.fast.ui.model.SizeConfig
import dev.meloda.fast.ui.model.ThemeConfig

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

val LocalThemeConfig = compositionLocalOf {
    ThemeConfig(
        darkMode = false,
        dynamicColors = false,
        selectedColorScheme = 0,
        amoledDark = false,
        enableBlur = false,
        enableMultiline = false,
        useSystemFont = false,
        enableAnimations = false
    )
}

val LocalSizeConfig = compositionLocalOf {
    SizeConfig(
        widthSize = DeviceSize.Compact,
        heightSize = DeviceSize.Compact
    )
}

val LocalHazeState = compositionLocalOf { HazeState(true) }
val LocalBottomPadding = compositionLocalOf { 0.dp }
val LocalUser = compositionLocalOf<VkUser?> { null }
val LocalReselectedTab = compositionLocalOf { mapOf<Any, Boolean>() }
val LocalNavRootController = compositionLocalOf<NavController?> { null }
val LocalNavController = compositionLocalOf<NavController?> { null }

@Composable
fun <T: NavController> ProvidableCompositionLocal<T?>.getOrThrow(): T {
    return requireNotNull(current)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    predefinedColorScheme: ColorScheme? = null,
    useDarkTheme: Boolean = false,
    useDynamicColors: Boolean = false,
    useAmoledBackground: Boolean = false,
    useSystemFont: Boolean = false,
    selectedColorScheme: Int = 0,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme: ColorScheme = when {
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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

    val colorPrimary by animateColorAsState(colorScheme.primary)
    val colorSurface by animateColorAsState(colorScheme.surface)
    val colorBackground by animateColorAsState(colorScheme.background)

    val typography = if (useSystemFont) {
        MaterialTheme.typography
    } else {
        MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = googleSansFonts),
            displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = googleSansFonts),
            displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = googleSansFonts),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = googleSansFonts),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = googleSansFonts),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = googleSansFonts),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = robotoFonts),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = robotoFonts),
            titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = robotoFonts),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = robotoFonts),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = robotoFonts),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = robotoFonts),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = robotoFonts),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = robotoFonts),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = robotoFonts),
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = (predefinedColorScheme ?: colorScheme)
            .copy(
                primary = colorPrimary,
                background = colorBackground,
                surface = colorSurface
            ),
        typography = typography,
        content = content
    )
}
