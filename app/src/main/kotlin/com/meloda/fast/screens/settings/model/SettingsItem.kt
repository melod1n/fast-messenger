package com.meloda.fast.screens.settings.model

import androidx.core.content.edit
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.base.UiText
import kotlin.properties.Delegates

sealed class SettingsItem<Value>(
    open val key: String,
) : AdapterDiffItem {

    var onTitleChanged: ((newTitle: UiText?) -> Unit)? = null

    var title: UiText? by Delegates.observable(null) { _, _, newValue ->
        onTitleChanged?.invoke(newValue)
    }

    var onSummaryChanged: ((newSummary: UiText?) -> Unit)? = null

    var summary: UiText? by Delegates.observable(null) { _, _, newValue ->
        onSummaryChanged?.invoke(newValue)
    }

    var onEnabledStateChanged: ((newEnabled: Boolean) -> Unit)? = null

    var isEnabled: Boolean by Delegates.observable(true) { _, _, newValue ->
        onEnabledStateChanged?.invoke(newValue)
    }

    var onVisibleStateChanged: ((newVisible: Boolean) -> Unit)? = null

    var isVisible: Boolean by Delegates.observable(true) { _, _, newValue ->
        onVisibleStateChanged?.invoke(newValue)
    }

    var onValueChanged: ((newValue: Value?) -> Unit)? = null

    var value: Value? by Delegates.observable(null) { _, oldValue, newValue ->
        if (key.trim().isEmpty() || oldValue == newValue) return@observable

        onValueChanged?.invoke(newValue)

        saveValueToPreferences(key, value)
    }

    private fun saveValueToPreferences(key: String, value: Any?) {
        AppGlobal.preferences.edit {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                else -> throw IllegalArgumentException("unknown class \"${value?.javaClass}\" with value \"$value\"")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> getValueFromPreferences(
        key: String,
        classToGet: Class<T>,
        defaultValue: Any?
    ): T? {
        val preferences = AppGlobal.preferences

        return when (classToGet) {
            String::class.java -> preferences.getString(key, defaultValue as? String)
            Boolean::class.java -> preferences.getBoolean(key, defaultValue as? Boolean == true)
            Int::class.java -> preferences.getInt(key, defaultValue as? Int ?: -1)
            Long::class.java -> preferences.getLong(key, defaultValue as? Long ?: -1)
            Float::class.java -> preferences.getFloat(key, defaultValue as? Float ?: -1f)
            else -> null
        }.let { value -> value as? T }
    }

    var defaultValue: Value? = null

    var titleProvider: TitleProvider<SettingsItem<Value>>? by Delegates.observable(null) { _, _, _ ->
        updateTitle()
    }

    var summaryProvider: SummaryProvider<SettingsItem<Value>>? by Delegates.observable(null) { _, _, _ ->
        updateSummary()
    }

    fun updateTitle() {
        titleProvider?.provideTitle(this)?.let { newTitle -> title = newTitle }
    }

    fun updateSummary() {
        summaryProvider?.provideSummary(this)?.let { newSummary -> summary = newSummary }
    }

    fun requireValue() = requireNotNull(value)

    override fun areItemsTheSame(newItem: AdapterDiffItem): Boolean {
        return newItem is SettingsItem<*> && newItem.key == this.key
    }

    fun interface TitleProvider<Item : SettingsItem<*>> {
        fun provideTitle(settingsItem: Item): UiText?
    }

    fun interface SummaryProvider<Item : SettingsItem<*>> {
        fun provideSummary(settingsItem: Item): UiText?
    }

    data class Title(override val key: String) : SettingsItem<Nothing>(key) {

        override val id: Int = -1

        companion object {
            fun build(
                key: String,
                title: UiText,
                isEnabled: Boolean = true,
                builder: Title.() -> Unit = {}
            ): Title {
                return Title(key).apply {
                    this.title = title
                    this.isEnabled = isEnabled
                }.apply(builder)
            }
        }
    }

    data class TitleSummary(override val key: String) : SettingsItem<String>(key) {

        override val id: Int = -1

        companion object {
            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                isEnabled: Boolean = true,
                builder: TitleSummary.() -> Unit = {}
            ): TitleSummary {
                return TitleSummary(key).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                }.apply(builder)
            }
        }
    }

    data class TextField(override val key: String) : SettingsItem<String>(key) {

        override val id: Int = -1

        companion object {
            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                defaultValue: String? = null,
                isEnabled: Boolean = true,
                builder: TextField.() -> Unit = {}
            ): TextField {
                return TextField(key).apply {
                    this.title = title
                    this.summary = summary
                    this.defaultValue = defaultValue
                    this.isEnabled = isEnabled
                    this.value = AppGlobal.preferences.getString(key, defaultValue)
                }.apply(builder)
            }
        }
    }

    data class Switch(override val key: String) : SettingsItem<Boolean>(key) {

        override val id: Int = -1

        companion object {

            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                isEnabled: Boolean = true,
                isChecked: Boolean? = null,
                defaultValue: Boolean? = null,
                builder: Switch.() -> Unit = {}
            ): Switch {
                return Switch(key).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                    this.defaultValue = defaultValue
                    this.value = defaultValue
                        ?.let { value -> AppGlobal.preferences.getBoolean(key, value) }
                        ?: isChecked
                }.apply(builder)
            }
        }
    }

    data class ListItem(override val key: String) : SettingsItem<Int>(key) {
        override val id: Int = -1

        var values: List<Int> = emptyList()
        var valueTitles: List<UiText> = emptyList()

        companion object {
            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                isEnabled: Boolean = true,
                values: List<Int>,
                valueTitles: List<UiText>,
                defaultValue: Int? = null,
                selectedIndex: Int? = null,
                builder: ListItem.() -> Unit = {}
            ): ListItem {
                return ListItem(key).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                    this.values = values
                    this.valueTitles = valueTitles

                    this.value = defaultValue
                        ?.let { value -> getValueFromPreferences(key, Int::class.java, value) }
                        ?: selectedIndex?.let { values[it] }
                }.apply(builder)
            }
        }
    }
}
