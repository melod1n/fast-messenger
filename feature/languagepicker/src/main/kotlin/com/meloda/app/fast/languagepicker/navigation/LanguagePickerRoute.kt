package com.meloda.app.fast.languagepicker.navigation

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.parseString
import com.meloda.app.fast.designsystem.R
import com.meloda.app.fast.languagepicker.LanguagePickerViewModel
import com.meloda.app.fast.languagepicker.LanguagePickerViewModelImpl
import com.meloda.app.fast.languagepicker.model.SelectableLanguage
import com.meloda.app.fast.languagepicker.presentation.LanguagePickerScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object LanguagePicker

private fun getLanguages(resources: Resources): List<SelectableLanguage> {
    return listOf(
        Triple(
            "",
            UiText.Resource(R.string.language_key_system),
            UiText.Resource(R.string.language_system)
        ),
        Triple(
            "en-US",
            UiText.Resource(R.string.language_key_english),
            UiText.Resource(R.string.language_english),
        ),
        Triple(
            "ru-RU",
            UiText.Resource(R.string.language_key_russian),
            UiText.Resource(R.string.language_russian)
        ),
        Triple(
            "uk-UA",
            UiText.Resource(R.string.language_key_ukrainian),
            UiText.Resource(R.string.language_ukrainian)
        )
    ).map { (key, language, local) ->
        Triple(
            key,
            language.parseString(resources).orEmpty(),
            local.parseString(resources).orEmpty()
        )
    }.map { (key, language, local) ->
        SelectableLanguage(
            local = local,
            language = language,
            key = key,
            isSelected = key == AppCompatDelegate.getApplicationLocales().toLanguageTags()
        )
    }
}

fun NavGraphBuilder.languagePickerRoute(
    onBack: () -> Unit,
) {
    composable<LanguagePicker> {
        val languages = getLanguages(LocalContext.current.resources)

        val viewModel: LanguagePickerViewModel = koinViewModel<LanguagePickerViewModelImpl>()
        viewModel.setLanguages(languages)

        LanguagePickerScreen(
            onBack = onBack,
            viewModel = viewModel
        )
    }
}

fun NavController.navigateToLanguagePicker() {
    this.navigate(LanguagePicker)
}
