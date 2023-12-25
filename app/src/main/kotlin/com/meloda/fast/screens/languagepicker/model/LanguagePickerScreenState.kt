package com.meloda.fast.screens.languagepicker.model

import androidx.compose.runtime.Immutable
import java.util.Locale

@Immutable
data class LanguagePickerScreenState(
    val languages: List<SelectableLanguage>,
    val currentLanguage: String,
    val isNeedToChangeLanguage: Boolean
) {

    companion object {
        val EMPTY: LanguagePickerScreenState = LanguagePickerScreenState(
            languages = emptyList(),
            currentLanguage = Locale.ENGLISH.language,
            isNeedToChangeLanguage = false
        )
    }
}
