package com.meloda.fast.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.meloda.fast.ext.isSystemUsingDarkMode
import com.meloda.fast.ui.colors.Blue

val BlueColorScheme
    get() = if (isSystemUsingDarkMode()) BlueDarkColorScheme
    else BlueLightColorScheme

val BlueLightColorScheme = lightColorScheme(
    primary = Blue.md_theme_light_primary,
    onPrimary = Blue.md_theme_light_onPrimary,
    primaryContainer = Blue.md_theme_light_primaryContainer,
    onPrimaryContainer = Blue.md_theme_light_onPrimaryContainer,
    secondary = Blue.md_theme_light_secondary,
    onSecondary = Blue.md_theme_light_onSecondary,
    secondaryContainer = Blue.md_theme_light_secondaryContainer,
    onSecondaryContainer = Blue.md_theme_light_onSecondaryContainer,
    tertiary = Blue.md_theme_light_tertiary,
    onTertiary = Blue.md_theme_light_onTertiary,
    tertiaryContainer = Blue.md_theme_light_tertiaryContainer,
    onTertiaryContainer = Blue.md_theme_light_onTertiaryContainer,
    error = Blue.md_theme_light_error,
    errorContainer = Blue.md_theme_light_errorContainer,
    onError = Blue.md_theme_light_onError,
    onErrorContainer = Blue.md_theme_light_onErrorContainer,
    background = Blue.md_theme_light_background,
    onBackground = Blue.md_theme_light_onBackground,
    surface = Blue.md_theme_light_surface,
    onSurface = Blue.md_theme_light_onSurface,
    surfaceVariant = Blue.md_theme_light_surfaceVariant,
    onSurfaceVariant = Blue.md_theme_light_onSurfaceVariant,
    outline = Blue.md_theme_light_outline,
    inverseOnSurface = Blue.md_theme_light_inverseOnSurface,
    inverseSurface = Blue.md_theme_light_inverseSurface,
    inversePrimary = Blue.md_theme_light_inversePrimary,
    surfaceTint = Blue.md_theme_light_surfaceTint,
    outlineVariant = Blue.md_theme_light_outlineVariant,
    scrim = Blue.md_theme_light_scrim,
)

val BlueDarkColorScheme = darkColorScheme(
    primary = Blue.md_theme_dark_primary,
    onPrimary = Blue.md_theme_dark_onPrimary,
    primaryContainer = Blue.md_theme_dark_primaryContainer,
    onPrimaryContainer = Blue.md_theme_dark_onPrimaryContainer,
    secondary = Blue.md_theme_dark_secondary,
    onSecondary = Blue.md_theme_dark_onSecondary,
    secondaryContainer = Blue.md_theme_dark_secondaryContainer,
    onSecondaryContainer = Blue.md_theme_dark_onSecondaryContainer,
    tertiary = Blue.md_theme_dark_tertiary,
    onTertiary = Blue.md_theme_dark_onTertiary,
    tertiaryContainer = Blue.md_theme_dark_tertiaryContainer,
    onTertiaryContainer = Blue.md_theme_dark_onTertiaryContainer,
    error = Blue.md_theme_dark_error,
    errorContainer = Blue.md_theme_dark_errorContainer,
    onError = Blue.md_theme_dark_onError,
    onErrorContainer = Blue.md_theme_dark_onErrorContainer,
    background = Blue.md_theme_dark_background,
    onBackground = Blue.md_theme_dark_onBackground,
    surface = Blue.md_theme_dark_surface,
    onSurface = Blue.md_theme_dark_onSurface,
    surfaceVariant = Blue.md_theme_dark_surfaceVariant,
    onSurfaceVariant = Blue.md_theme_dark_onSurfaceVariant,
    outline = Blue.md_theme_dark_outline,
    inverseOnSurface = Blue.md_theme_dark_inverseOnSurface,
    inverseSurface = Blue.md_theme_dark_inverseSurface,
    inversePrimary = Blue.md_theme_dark_inversePrimary,
    surfaceTint = Blue.md_theme_dark_surfaceTint,
    outlineVariant = Blue.md_theme_dark_outlineVariant,
    scrim = Blue.md_theme_dark_scrim,
)
