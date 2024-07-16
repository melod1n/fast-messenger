package dev.meloda.fast.settings.model

import androidx.compose.runtime.Immutable

typealias StringList = List<String>
typealias TypeList<T> = List<T>

@Immutable
sealed class UiItem(open val key: String) {

    data class Title(
        override val key: String,
        val title: String,
        val isVisible: Boolean
    ) : UiItem(key)

    data class TitleText(
        override val key: String,
        val title: String?,
        val text: String?,
        val isVisible: Boolean,
        val isEnabled: Boolean
    ) : UiItem(key)

    data class Switch(
        override val key: String,
        val title: String?,
        val text: String?,
        val isVisible: Boolean,
        val isEnabled: Boolean,
        val isChecked: Boolean
    ) : UiItem(key)

    data class TextField(
        override val key: String,
        val title: String?,
        val text: String?,
        val isVisible: Boolean,
        val isEnabled: Boolean,
        val fieldText: String
    ) : UiItem(key)

    data class List<T>(
        override val key: String,
        val title: String?,
        val text: String?,
        val isVisible: Boolean,
        val isEnabled: Boolean,
        val selectedValue: T,
        val titles: StringList,
        val values: TypeList<T>
    ) : UiItem(key)
}
