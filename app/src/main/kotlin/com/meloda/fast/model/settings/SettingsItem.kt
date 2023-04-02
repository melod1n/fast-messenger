package com.meloda.fast.model.settings

import androidx.core.content.edit
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.model.base.AdapterDiffItem
import kotlin.properties.Delegates

sealed class SettingsItem<Value>(
    open val key: String,
) : AdapterDiffItem {

    var onTitleChanged: ((newTitle: String?) -> Unit)? = null

    var title: String? by Delegates.observable(null) { _, _, newValue ->
        onTitleChanged?.invoke(newValue)
    }

    var onSummaryChanged: ((newSummary: String?) -> Unit)? = null

    var summary: String? by Delegates.observable(null) { _, _, newValue ->
        onSummaryChanged?.invoke(newValue)
    }

    var onEnabledStateChanged: ((isEnabled: Boolean) -> Unit)? = null

    var isEnabled: Boolean by Delegates.observable(true) { _, _, newValue ->
        onEnabledStateChanged?.invoke(newValue)
    }

    var value: Value? by Delegates.observable(null) { _, oldValue, newValue ->
        if (key.trim().isEmpty() || oldValue == newValue) return@observable

        saveValueToPreferences(key, value)
    }

    protected fun saveValueToPreferences(key: String, value: Any?) {
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
        fun provideTitle(settingsItem: Item): String?
    }

    fun interface SummaryProvider<Item : SettingsItem<*>> {
        fun provideSummary(settingsItem: Item): String?
    }

    data class Title(override val key: String) : SettingsItem<Nothing>(key) {

        override val id: Int = -1

        companion object {
            fun build(
                key: String,
                title: String,
                isEnabled: Boolean = true
            ): Title {
                return Title(key).apply {
                    this.title = title
                    this.isEnabled = isEnabled
                }
            }
        }
    }

    data class TitleSummary(override val key: String) : SettingsItem<String>(key) {

        override val id: Int = -1

        companion object {
            fun build(
                key: String,
                title: String? = null,
                summary: String? = null,
                isEnabled: Boolean = true
            ): TitleSummary {
                return TitleSummary(key).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                }
            }
        }
    }

    data class EditText(override val key: String) : SettingsItem<String>(key) {

        override val id: Int = -1

        companion object {
            fun build(
                key: String,
                title: String? = null,
                summary: String? = null,
                defaultValue: String? = null,
                isEnabled: Boolean = true
            ): EditText {
                return EditText(key).apply {
                    this.title = title
                    this.summary = summary
                    this.defaultValue = defaultValue
                    this.isEnabled = isEnabled
                    this.value = AppGlobal.preferences.getString(key, defaultValue)
                }
            }
        }
    }

    data class Switch(override val key: String) : SettingsItem<Boolean>(key) {

        override val id: Int = -1

        companion object {

            fun build(
                key: String,
                title: String? = null,
                summary: String? = null,
                isEnabled: Boolean = true,
                isChecked: Boolean? = null,
                defaultValue: Boolean? = null,
            ): Switch {
                return Switch(key).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                    this.defaultValue = defaultValue
                    this.value = defaultValue
                        ?.let { value -> AppGlobal.preferences.getBoolean(key, value) }
                        ?: isChecked
                }
            }
        }
    }

    data class ListItem(override val key: String) : SettingsItem<Int>(key) {
        override val id: Int = -1

        var values: List<Int> = emptyList()
        var valueTitles: List<String> = emptyList()

        companion object {
            fun build(
                key: String,
                title: String? = null,
                summary: String? = null,
                isEnabled: Boolean = true,
                values: List<Int>,
                valueTitles: List<String>,
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
