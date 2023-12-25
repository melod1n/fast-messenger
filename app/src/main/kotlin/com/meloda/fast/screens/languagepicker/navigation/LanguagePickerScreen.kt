package com.meloda.fast.screens.languagepicker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.languagepicker.LanguagePickerViewModel
import com.meloda.fast.screens.languagepicker.LanguagePickerViewModelImpl
import com.meloda.fast.screens.languagepicker.presentation.LanguagePickerScreenContent
import com.meloda.fast.screens.settings.UserSettings
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

object LanguagePickerScreen : Screen {

    // TODO: 25/12/2023, Danil Nikolaev get rid of hardcode
    private val languages = mapOf(
        "System" to "system",
        "English" to "en",
        "Russian" to "ru",
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: LanguagePickerViewModel = koinViewModel<LanguagePickerViewModelImpl>()
        val userSettings: UserSettings = koinInject()

        val language by userSettings.language.collectAsStateWithLifecycle()

        // TODO: 25/12/2023, Danil Nikolaev: fix setting value after updating ui
        LaunchedEffect(Unit) {
            viewModel.setLanguages(
                locales = languages
                ,
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
}
