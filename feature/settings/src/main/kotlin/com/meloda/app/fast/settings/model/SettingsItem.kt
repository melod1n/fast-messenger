package com.meloda.app.fast.settings.model

import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.datastore.SettingsController
import kotlin.properties.Delegates

// TODO: 24/12/2023, Danil Nikolaev: refactor
sealed class SettingsItem<Value>(
    open val key: String,
) {
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

        SettingsController.put(key, newValue)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> getValueFromPreferences(
        key: String,
        classToGet: Class<T>,
        defaultValue: Any?
    ): T? {
        return when (classToGet) {
            String::class.java -> SettingsController.getString(key, defaultValue as? String)

            Boolean::class.java -> {
                SettingsController.getBoolean(key, defaultValue as? Boolean == true)
            }

            Int::class.java -> SettingsController.getInt(key, defaultValue as? Int ?: -1)
            Long::class.java -> SettingsController.getLong(key, defaultValue as? Long ?: -1)
            Float::class.java -> SettingsController.getFloat(key, defaultValue as? Float ?: -1f)
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

    fun interface TitleProvider<Item : SettingsItem<*>> {
        fun provideTitle(settingsItem: Item): UiText?
    }

    fun interface SummaryProvider<Item : SettingsItem<*>> {
        fun provideSummary(settingsItem: Item): UiText?
    }

    data class Title(override val key: String) : SettingsItem<Nothing>(key) {

        companion object {
            fun build(
                key: String,
                title: UiText,
                isEnabled: Boolean = true,
                isVisible: Boolean = true,
                builder: Title.() -> Unit = {}
            ): Title {
                return Title(key).apply {
                    this.title = title
                    this.isEnabled = isEnabled
                    this.isVisible = isVisible
                }.apply(builder)
            }
        }
    }

    data class TitleSummary(override val key: String) : SettingsItem<String>(key) {

        companion object {
            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                isEnabled: Boolean = true,
                isVisible: Boolean = true,
                builder: TitleSummary.() -> Unit = {}
            ): TitleSummary {
                return TitleSummary(key).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                    this.isVisible = isVisible
                }.apply(builder)
            }
        }
    }

    data class TextField(override val key: String) : SettingsItem<String>(key) {

        companion object {
            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                defaultValue: String? = null,
                isEnabled: Boolean = true,
                isVisible: Boolean = true,
                builder: TextField.() -> Unit = {}
            ): TextField {
                return TextField(key).apply {
                    this.title = title
                    this.summary = summary
                    this.defaultValue = defaultValue
                    this.isEnabled = isEnabled
                    this.isVisible = isVisible

                    this.value = SettingsController.getString(key, defaultValue)
                }.apply(builder)
            }
        }
    }

    data class Switch(override val key: String) : SettingsItem<Boolean>(key) {

        companion object {

            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                isEnabled: Boolean = true,
                isChecked: Boolean? = null,
                isVisible: Boolean = true,
                defaultValue: Boolean? = null,
                builder: Switch.() -> Unit = {}
            ): Switch {
                return Switch(key).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                    this.defaultValue = defaultValue
                    this.isVisible = isVisible

                    this.value = defaultValue
                        ?.let { value -> SettingsController.getBoolean(key, value) }
                        ?: isChecked
                }.apply(builder)
            }
        }
    }

    data class ListItem(
        override val key: String,
        val values: List<Int>,
        val valueTitles: List<UiText>
    ) : SettingsItem<Int>(key) {

        companion object {
            fun build(
                key: String,
                title: UiText? = null,
                summary: UiText? = null,
                isEnabled: Boolean = true,
                isVisible: Boolean = true,
                values: List<Int>,
                valueTitles: List<UiText>,
                defaultValue: Int? = null,
                selectedIndex: Int? = null,
                builder: ListItem.() -> Unit = {}
            ): ListItem {
                return ListItem(
                    key = key,
                    values = values,
                    valueTitles = valueTitles
                ).apply {
                    this.title = title
                    this.summary = summary
                    this.isEnabled = isEnabled
                    this.isVisible = isVisible

                    this.value = defaultValue
                        ?.let { value -> getValueFromPreferences(key, Int::class.java, value) }
                        ?: selectedIndex?.let { values[it] }
                }.apply(builder)
            }
        }
    }
}