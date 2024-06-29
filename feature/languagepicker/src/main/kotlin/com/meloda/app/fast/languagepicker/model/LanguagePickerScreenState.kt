package com.meloda.app.fast.languagepicker.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.designsystem.ImmutableList

@Immutable
data class LanguagePickerScreenState(
    val languages: ImmutableList<SelectableLanguage>,
    val currentLanguage: String?,
)
