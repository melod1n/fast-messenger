package com.meloda.app.fast.languagepicker.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.languagepicker.presentation.LanguagePickerScreen
import kotlinx.serialization.Serializable

@Serializable
object LanguagePicker

fun NavGraphBuilder.languagePickerRoute(
    onBack: () -> Unit,
) {
    composable<LanguagePicker> {
        LanguagePickerScreen(onBack = onBack)
    }
}

fun NavController.navigateToLanguagePicker() {
    this.navigate(LanguagePicker)
}
