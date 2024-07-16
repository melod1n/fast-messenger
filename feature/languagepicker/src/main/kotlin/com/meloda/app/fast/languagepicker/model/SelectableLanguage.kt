package dev.meloda.fast.languagepicker.model

data class SelectableLanguage(
    val local: String,
    val language: String,
    val key: String,
    val isSelected: Boolean
)
