package com.meloda.app.fast.languagepicker.model

import androidx.compose.runtime.Immutable

@Immutable
data class LanguagePickerScreenState(
    val languages: List<SelectableLanguage>,
    val currentLanguage: String?,
    val isNeedToChangeLanguage: Boolean
) {

    companion object {
        val EMPTY: LanguagePickerScreenState = LanguagePickerScreenState(
            languages = emptyList(),
            currentLanguage = null,
            isNeedToChangeLanguage = false
        )
    }
}
