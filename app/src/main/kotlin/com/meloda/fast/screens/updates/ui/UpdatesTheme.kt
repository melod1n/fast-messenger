package com.meloda.fast.screens.updates.ui

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.isUsingDynamicColors

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun UpdatesTheme(
    darkTheme: Boolean = isUsingDarkTheme(),
    dynamicColor: Boolean = isUsingDynamicColors(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography
    ) {
        content.invoke()
//        CompositionLocalProvider(
//            LocalRippleTheme provides JetNewsRippleTheme,
//            content = content
//        )
    }
}
//
//private object JetNewsRippleTheme : RippleTheme {
//
//    @Composable
//    override fun defaultColor(): Color = Color(255, 0, 0)
//
//    @Composable
//    override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
//        contentColor = Color.Red,
//        lightTheme = !isUsingDarkTheme()
//    )
//}
