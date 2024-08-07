package dev.meloda.fast.languagepicker.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.languagepicker.presentation.LanguagePickerRoute
import kotlinx.serialization.Serializable

@Serializable
object LanguagePicker

fun NavGraphBuilder.languagePickerScreen(
    onBack: () -> Unit,
) {
    composable<LanguagePicker> {
        LanguagePickerRoute(onBack = onBack)
    }
}

fun NavController.navigateToLanguagePicker() {
    this.navigate(LanguagePicker)
}
