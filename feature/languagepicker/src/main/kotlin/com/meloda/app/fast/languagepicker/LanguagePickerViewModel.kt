package com.meloda.app.fast.languagepicker

import androidx.lifecycle.ViewModel
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.designsystem.ImmutableList
import com.meloda.app.fast.languagepicker.model.LanguagePickerScreenState
import com.meloda.app.fast.languagepicker.model.SelectableLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface LanguagePickerViewModel {
    val screenState: StateFlow<LanguagePickerScreenState>

    fun setLanguages(
        locales: Map<String, String>,
        currentCode: String
    )

    fun onLanguagePicked(position: Int)

    fun onApplyButtonClicked()
}

class LanguagePickerViewModelImpl(
    private val userSettings: UserSettings
) : LanguagePickerViewModel, ViewModel() {

    override val screenState = MutableStateFlow(
        LanguagePickerScreenState(
            languages = ImmutableList.empty(),
            currentLanguage = userSettings.language.value
        )
    )

    override fun setLanguages(
        locales: Map<String, String>,
        currentCode: String
    ) {
        val codes = locales.keys.toList()

        val selectableLanguages = codes.map { code ->
            SelectableLanguage(
                language = locales[code].orEmpty(),
                key = code,
                isSelected = code == currentCode
            )
        }

        screenState.setValue { old ->
            old.copy(
                languages = ImmutableList.copyOf(selectableLanguages),
                currentLanguage = currentCode
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
        val selectableLanguage =
            screenState.value.languages.singleOrNull(SelectableLanguage::isSelected)

        if (selectableLanguage != null) {
            val newCode = selectableLanguage.key
            userSettings.setLanguage(newCode)
            screenState.setValue { old -> old.copy(currentLanguage = newCode) }
        }
    }
}
