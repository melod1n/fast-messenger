package com.meloda.app.fast.languagepicker.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.parseString
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.languagepicker.LanguagePickerViewModel
import com.meloda.app.fast.languagepicker.LanguagePickerViewModelImpl
import com.meloda.app.fast.languagepicker.presentation.LanguagePickerScreenContent
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.meloda.app.fast.designsystem.R as UiR

object LanguagePickerScreen : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: LanguagePickerViewModel = koinViewModel<LanguagePickerViewModelImpl>()
        val userSettings: UserSettings = koinInject()

        val language by userSettings.language.collectAsStateWithLifecycle()

        LaunchedEffect(true) {
            viewModel.setLanguages(
                locales = getLanguages(context),
                currentLanguage = language
            )
        }

        LanguagePickerScreenContent(
            onLanguagePicked = { newLanguage ->
                userSettings.setLanguage(newLanguage)
            },
            onBackClick = navigator::pop,
            viewModel = viewModel
        )
    }

    // TODO: 05/05/2024, Danil Nikolaev: remove or improve
    private fun getLanguages(context: Context): Map<String, String> {
        return listOf(
            UiText.Resource(UiR.string.language_system) to "system",
            UiText.Resource(UiR.string.language_english) to "en",
            UiText.Resource(UiR.string.language_russian) to "ru",
            UiText.Resource(UiR.string.language_ukrainian) to "uk"
        ).associate { pair ->
            pair.first.parseString(context).orEmpty() to pair.second
        }
    }
}
