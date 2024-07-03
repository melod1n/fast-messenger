package com.meloda.app.fast.languagepicker

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.languagepicker.model.LanguagePickerScreenState
import com.meloda.app.fast.languagepicker.model.SelectableLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface LanguagePickerViewModel {
    val screenState: StateFlow<LanguagePickerScreenState>

    fun setLanguages(languages: List<SelectableLanguage>)

    fun onLanguagePicked(newLanguage: SelectableLanguage)

    fun onApplyButtonClicked()

    fun updateCurrentLocale(locale: String)
}

class LanguagePickerViewModelImpl : LanguagePickerViewModel, ViewModel() {

    override val screenState = MutableStateFlow(
        LanguagePickerScreenState(
            languages = emptyList(),
            currentLanguage = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        )
    )

    override fun setLanguages(languages: List<SelectableLanguage>) {
        screenState.setValue { old -> old.copy(languages = languages) }
    }

    override fun onLanguagePicked(newLanguage: SelectableLanguage) {
        val newList = screenState.value.languages.map { language ->
            language.copy(isSelected = language.key == newLanguage.key)
        }

        screenState.setValue { old -> old.copy(languages = newList) }
    }

    override fun onApplyButtonClicked() {
        val selectableLanguage =
            screenState.value.languages.singleOrNull(SelectableLanguage::isSelected)

        if (selectableLanguage != null) {
            val newCode = selectableLanguage.key
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newCode))
            screenState.setValue { old -> old.copy(currentLanguage = newCode) }
        }
    }

    override fun updateCurrentLocale(locale: String) {
        val selected = screenState.value.languages.singleOrNull(SelectableLanguage::isSelected)

        if (selected != null) {
            if (AppCompatDelegate.getApplicationLocales()
                    .getFirstMatch(arrayOf(selected.key))?.language == locale
            ) {
                return
            }
        }

        screenState.setValue { old ->
            old.copy(
                languages = old.languages.map { language ->
                    language.copy(isSelected = language.key == locale)
                },
                currentLanguage = locale
            )
        }
    }
}
