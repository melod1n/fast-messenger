package dev.meloda.fast.languagepicker.model

import androidx.compose.runtime.Immutable

@Immutable
data class LanguagePickerScreenState(
    val languages: List<SelectableLanguage>,
    val currentLanguage: String?,
) {

    companion object {
        val EMPTY: LanguagePickerScreenState = LanguagePickerScreenState(
            languages = emptyList(),
            currentLanguage = null
        )
    }
}
