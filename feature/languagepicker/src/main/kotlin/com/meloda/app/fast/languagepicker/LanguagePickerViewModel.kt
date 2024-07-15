package com.meloda.app.fast.languagepicker

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.parseString
import com.meloda.app.fast.languagepicker.model.LanguagePickerScreenState
import com.meloda.app.fast.languagepicker.model.SelectableLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import com.meloda.app.fast.ui.R as UiR

interface LanguagePickerViewModel {
    val screenState: StateFlow<LanguagePickerScreenState>

    fun onLanguagePicked(newLanguage: SelectableLanguage)
    fun onApplyButtonClicked()
    fun updateCurrentLocale(locale: String)
}

class LanguagePickerViewModelImpl(
    private val resources: Resources
) : LanguagePickerViewModel, ViewModel() {

    override val screenState = MutableStateFlow(LanguagePickerScreenState.EMPTY)

    init {
        val languages = listOf(
            Triple(
                "",
                UiText.Resource(UiR.string.language_key_system),
                UiText.Resource(UiR.string.language_system)
            ),
            Triple(
                "en-US",
                UiText.Resource(UiR.string.language_key_english),
                UiText.Resource(UiR.string.language_english),
            ),
            Triple(
                "ru-RU",
                UiText.Resource(UiR.string.language_key_russian),
                UiText.Resource(UiR.string.language_russian)
            ),
            Triple(
                "uk-UA",
                UiText.Resource(UiR.string.language_key_ukrainian),
                UiText.Resource(UiR.string.language_ukrainian)
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
