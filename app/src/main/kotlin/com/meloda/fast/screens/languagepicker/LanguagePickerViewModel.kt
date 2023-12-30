package com.meloda.fast.screens.languagepicker

import androidx.lifecycle.ViewModel
import com.conena.nanokt.android.content.put
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.setValue
import com.meloda.fast.screens.languagepicker.model.LanguagePickerScreenState
import com.meloda.fast.screens.languagepicker.model.SelectableLanguage
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.screens.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface LanguagePickerViewModel {
    val screenState: StateFlow<LanguagePickerScreenState>

    fun setLanguages(
        locales: Map<String, String>,
        currentLanguage: String
    )

    fun onLanguagePicked(position: Int)

    fun onApplyButtonClicked()

    fun onLanguageChanged()
}

class LanguagePickerViewModelImpl(
    userSettings: UserSettings
) : LanguagePickerViewModel, ViewModel() {

    override val screenState = MutableStateFlow(
        LanguagePickerScreenState.EMPTY.copy(
            currentLanguage = userSettings.language.value
        )
    )

    override fun setLanguages(
        locales: Map<String, String>,
        currentLanguage: String
    ) {
        val languages = locales.keys.toList()

        val selectableLanguages = languages.map { language ->
            SelectableLanguage(
                language = language,
                key = locales[language].orEmpty(),
                isSelected = locales[language] == currentLanguage
            )
        }

        screenState.setValue { old ->
            old.copy(
                languages = selectableLanguages,
                currentLanguage = currentLanguage
            )
        }
    }

    override fun onLanguagePicked(position: Int) {
        val newList = screenState.value.languages.mapIndexed { index, language ->
            language.copy(isSelected = index == position)
        }

        screenState.setValue { old -> old.copy(languages = newList) }
    }

    override fun onApplyButtonClicked() {
        AppGlobal.preferences.put(
            SettingsKeys.KEY_APPEARANCE_LANGUAGE,
            screenState.value.languages.find(SelectableLanguage::isSelected)?.key ?: "en"
        )

        screenState.setValue { old -> old.copy(isNeedToChangeLanguage = true) }
    }

    override fun onLanguageChanged() {
        screenState.setValue { old -> old.copy(isNeedToChangeLanguage = false) }
    }
}
