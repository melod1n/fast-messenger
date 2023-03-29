package com.meloda.fast.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ui.colors.Green

val GreenColorScheme
    get() = if (isUsingDarkTheme()) GreenDarkColorScheme
    else GreenLightColorScheme

val GreenLightColorScheme = lightColorScheme(
    primary = Green.md_theme_light_primary,
    onPrimary = Green.md_theme_light_onPrimary,
    primaryContainer = Green.md_theme_light_primaryContainer,
    onPrimaryContainer = Green.md_theme_light_onPrimaryContainer,
    secondary = Green.md_theme_light_secondary,
    onSecondary = Green.md_theme_light_onSecondary,
    secondaryContainer = Green.md_theme_light_secondaryContainer,
    onSecondaryContainer = Green.md_theme_light_onSecondaryContainer,
    tertiary = Green.md_theme_light_tertiary,
    onTertiary = Green.md_theme_light_onTertiary,
    tertiaryContainer = Green.md_theme_light_tertiaryContainer,
    onTertiaryContainer = Green.md_theme_light_onTertiaryContainer,
    error = Green.md_theme_light_error,
    errorContainer = Green.md_theme_light_errorContainer,
    onError = Green.md_theme_light_onError,
    onErrorContainer = Green.md_theme_light_onErrorContainer,
    background = Green.md_theme_light_background,
    onBackground = Green.md_theme_light_onBackground,
    surface = Green.md_theme_light_surface,
    onSurface = Green.md_theme_light_onSurface,
    surfaceVariant = Green.md_theme_light_surfaceVariant,
    onSurfaceVariant = Green.md_theme_light_onSurfaceVariant,
    outline = Green.md_theme_light_outline,
    inverseOnSurface = Green.md_theme_light_inverseOnSurface,
    inverseSurface = Green.md_theme_light_inverseSurface,
    inversePrimary = Green.md_theme_light_inversePrimary,
    surfaceTint = Green.md_theme_light_surfaceTint,
    outlineVariant = Green.md_theme_light_outlineVariant,
    scrim = Green.md_theme_light_scrim,
)

val GreenDarkColorScheme = darkColorScheme(
    primary = Green.md_theme_dark_primary,
    onPrimary = Green.md_theme_dark_onPrimary,
    primaryContainer = Green.md_theme_dark_primaryContainer,
    onPrimaryContainer = Green.md_theme_dark_onPrimaryContainer,
    secondary = Green.md_theme_dark_secondary,
    onSecondary = Green.md_theme_dark_onSecondary,
    secondaryContainer = Green.md_theme_dark_secondaryContainer,
    onSecondaryContainer = Green.md_theme_dark_onSecondaryContainer,
    tertiary = Green.md_theme_dark_tertiary,
    onTertiary = Green.md_theme_dark_onTertiary,
    tertiaryContainer = Green.md_theme_dark_tertiaryContainer,
    onTertiaryContainer = Green.md_theme_dark_onTertiaryContainer,
    error = Green.md_theme_dark_error,
    errorContainer = Green.md_theme_dark_errorContainer,
    onError = Green.md_theme_dark_onError,
    onErrorContainer = Green.md_theme_dark_onErrorContainer,
    background = Green.md_theme_dark_background,
    onBackground = Green.md_theme_dark_onBackground,
    surface = Green.md_theme_dark_surface,
    onSurface = Green.md_theme_dark_onSurface,
    surfaceVariant = Green.md_theme_dark_surfaceVariant,
    onSurfaceVariant = Green.md_theme_dark_onSurfaceVariant,
    outline = Green.md_theme_dark_outline,
    inverseOnSurface = Green.md_theme_dark_inverseOnSurface,
    inverseSurface = Green.md_theme_dark_inverseSurface,
    inversePrimary = Green.md_theme_dark_inversePrimary,
    surfaceTint = Green.md_theme_dark_surfaceTint,
    outlineVariant = Green.md_theme_dark_outlineVariant,
    scrim = Green.md_theme_dark_scrim,
)
