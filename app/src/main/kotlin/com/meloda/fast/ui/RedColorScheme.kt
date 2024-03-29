package com.meloda.fast.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.meloda.fast.ext.isSystemUsingDarkMode
import com.meloda.fast.ui.colors.Red

val RedColorScheme
    get() = if (isSystemUsingDarkMode()) RedDarkColorScheme
    else RedLightColorScheme

val RedLightColorScheme = lightColorScheme(
    primary = Red.md_theme_light_primary,
    onPrimary = Red.md_theme_light_onPrimary,
    primaryContainer = Red.md_theme_light_primaryContainer,
    onPrimaryContainer = Red.md_theme_light_onPrimaryContainer,
    secondary = Red.md_theme_light_secondary,
    onSecondary = Red.md_theme_light_onSecondary,
    secondaryContainer = Red.md_theme_light_secondaryContainer,
    onSecondaryContainer = Red.md_theme_light_onSecondaryContainer,
    tertiary = Red.md_theme_light_tertiary,
    onTertiary = Red.md_theme_light_onTertiary,
    tertiaryContainer = Red.md_theme_light_tertiaryContainer,
    onTertiaryContainer = Red.md_theme_light_onTertiaryContainer,
    error = Red.md_theme_light_error,
    errorContainer = Red.md_theme_light_errorContainer,
    onError = Red.md_theme_light_onError,
    onErrorContainer = Red.md_theme_light_onErrorContainer,
    background = Red.md_theme_light_background,
    onBackground = Red.md_theme_light_onBackground,
    surface = Red.md_theme_light_surface,
    onSurface = Red.md_theme_light_onSurface,
    surfaceVariant = Red.md_theme_light_surfaceVariant,
    onSurfaceVariant = Red.md_theme_light_onSurfaceVariant,
    outline = Red.md_theme_light_outline,
    inverseOnSurface = Red.md_theme_light_inverseOnSurface,
    inverseSurface = Red.md_theme_light_inverseSurface,
    inversePrimary = Red.md_theme_light_inversePrimary,
    surfaceTint = Red.md_theme_light_surfaceTint,
    outlineVariant = Red.md_theme_light_outlineVariant,
    scrim = Red.md_theme_light_scrim,
)

val RedDarkColorScheme = darkColorScheme(
    primary = Red.md_theme_dark_primary,
    onPrimary = Red.md_theme_dark_onPrimary,
    primaryContainer = Red.md_theme_dark_primaryContainer,
    onPrimaryContainer = Red.md_theme_dark_onPrimaryContainer,
    secondary = Red.md_theme_dark_secondary,
    onSecondary = Red.md_theme_dark_onSecondary,
    secondaryContainer = Red.md_theme_dark_secondaryContainer,
    onSecondaryContainer = Red.md_theme_dark_onSecondaryContainer,
    tertiary = Red.md_theme_dark_tertiary,
    onTertiary = Red.md_theme_dark_onTertiary,
    tertiaryContainer = Red.md_theme_dark_tertiaryContainer,
    onTertiaryContainer = Red.md_theme_dark_onTertiaryContainer,
    error = Red.md_theme_dark_error,
    errorContainer = Red.md_theme_dark_errorContainer,
    onError = Red.md_theme_dark_onError,
    onErrorContainer = Red.md_theme_dark_onErrorContainer,
    background = Red.md_theme_dark_background,
    onBackground = Red.md_theme_dark_onBackground,
    surface = Red.md_theme_dark_surface,
    onSurface = Red.md_theme_dark_onSurface,
    surfaceVariant = Red.md_theme_dark_surfaceVariant,
    onSurfaceVariant = Red.md_theme_dark_onSurfaceVariant,
    outline = Red.md_theme_dark_outline,
    inverseOnSurface = Red.md_theme_dark_inverseOnSurface,
    inverseSurface = Red.md_theme_dark_inverseSurface,
    inversePrimary = Red.md_theme_dark_inversePrimary,
    surfaceTint = Red.md_theme_dark_surfaceTint,
    outlineVariant = Red.md_theme_dark_outlineVariant,
    scrim = Red.md_theme_dark_scrim,
)
