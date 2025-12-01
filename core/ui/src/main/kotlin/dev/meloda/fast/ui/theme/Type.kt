package dev.meloda.fast.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.meloda.fast.ui.R

val GoogleSansFamily = FontFamily(
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

val RobotoFamily = FontFamily(
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
