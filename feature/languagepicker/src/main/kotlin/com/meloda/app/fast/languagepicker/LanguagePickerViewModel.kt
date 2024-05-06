package com.meloda.app.fast.languagepicker

import androidx.lifecycle.ViewModel
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.languagepicker.model.LanguagePickerScreenState
import com.meloda.app.fast.languagepicker.model.SelectableLanguage
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
        // TODO: 05/05/2024, Danil Nikolaev: implement
//        AppGlobal.preferences.put(
//            SettingsKeys.KEY_APPEARANCE_LANGUAGE,
//            screenState.value.languages.find(SelectableLanguage::isSelected)?.key ?: "en"
//        )

        screenState.setValue { old -> old.copy(isNeedToChangeLanguage = true) }
    }

    override fun onLanguageChanged() {
        screenState.setValue { old -> old.copy(isNeedToChangeLanguage = false) }
    }
}
