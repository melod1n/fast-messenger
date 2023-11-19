package com.meloda.fast.ui

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.meloda.fast.R
import com.meloda.fast.ext.isSystemUsingDarkMode
import com.meloda.fast.ext.isUsingDarkThemeComposable
import com.meloda.fast.ext.isUsingDynamicColorsComposable


val StandardColorScheme
    get() = if (isSystemUsingDarkMode()) DarkColorScheme
    else LightColorScheme

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

@Composable
fun dynamicColorScheme(): ColorScheme {
    val context = LocalContext.current
    return if (isSystemUsingDarkMode()) dynamicDarkColorScheme(context)
    else dynamicLightColorScheme(context)
}

private val googleSansFonts = FontFamily(
    Font(R.font.google_sans_regular),
    Font(R.font.google_sans_italic, style = FontStyle.Italic),
    Font(R.font.google_sans_medium, weight = FontWeight.Medium),
    Font(
        R.font.google_sans_medium_italic,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(R.font.google_sans_bold, weight = FontWeight.Bold),
    Font(
        R.font.google_sans_bold_italic,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    ),
)

private val robotoFonts = FontFamily(
    Font(R.font.roboto_regular),
    // TODO: 27.03.2023, Danil Nikolaev: add all roboto fonts
)

@Composable
fun AppTheme(
    predefinedColorScheme: ColorScheme? = null,
    useDarkTheme: Boolean = isUsingDarkThemeComposable(),
    useDynamicColors: Boolean = isUsingDynamicColorsComposable(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when {
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
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

    MaterialTheme(
        colorScheme = predefinedColorScheme ?: colorScheme,
        typography = typography,
        content = content
    )
}
