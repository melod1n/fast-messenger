package com.meloda.fast.screens.languagepicker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.ext.getLanguages
import com.meloda.fast.screens.languagepicker.LanguagePickerViewModel
import com.meloda.fast.screens.languagepicker.LanguagePickerViewModelImpl
import com.meloda.fast.screens.languagepicker.presentation.LanguagePickerScreenContent
import com.meloda.fast.screens.settings.UserSettings
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

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
}
