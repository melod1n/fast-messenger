package com.meloda.fast.model.settings

import androidx.core.content.edit
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.model.base.AdapterDiffItem
import kotlin.properties.Delegates

sealed class SettingsItem<T>(val key: String) : AdapterDiffItem {

    var value: T? by Delegates.observable(null) { _, oldValue, newValue ->
        if (key.trim().isEmpty() || oldValue == newValue) return@observable

        AppGlobal.preferences.edit {
            when (this@SettingsItem) {
                is TitleSummary -> putString(key, value)
                is EditText -> putString(key, value)
                is CheckBox -> putBoolean(key, value == true)
                is Switch -> putBoolean(key, value == true)
                else -> Unit
            }
        }
    }

    fun requireValue() = requireNotNull(value)

    override fun areItemsTheSame(newItem: AdapterDiffItem): Boolean {
        return newItem is SettingsItem<*> && newItem.key == this.key
    }

    interface TitleProvider<T : SettingsItem<*>> {
        fun provideTitle(settingsItem: T): String?
    }

    interface SummaryProvider<T : SettingsItem<*>> {
        fun provideSummary(settingsItem: T): String?
    }

    data class Title(
        val title: String,
        val itemKey: String = "",
    ) : SettingsItem<Nothing>(itemKey) {

        override val id: Int = -1

        companion object {
            const val ItemType = 1
        }
    }

    data class TitleSummary(
        val itemKey: String,
        val title: String? = null,
        val summary: String? = null,
    ) : SettingsItem<String>(itemKey) {

        override val id: Int = -1

        companion object {
            const val ItemType = 2
        }
    }

    data class EditText(
        val itemKey: String,
        var title: String? = null,
        var summary: String? = null,
        val defaultValue: String? = null,
    ) : SettingsItem<String>(itemKey) {

        override val id: Int = -1

        var titleProvider: TitleProvider<EditText>? by Delegates.observable(null) { _, _, _ ->
            updateTitle()
        }

        var summaryProvider: SummaryProvider<EditText>? by Delegates.observable(null) { _, _, _ ->
            updateSummary()
        }

        fun updateTitle() {
            title = titleProvider?.provideTitle(this)
        }

        fun updateSummary() {
            summary = summaryProvider?.provideSummary(this)
        }

        class SimpleTitleProvider : TitleProvider<EditText> {
            override fun provideTitle(settingsItem: EditText): String? {
                return settingsItem.title
            }
        }

        class SimpleSummaryProvider : SummaryProvider<EditText> {
            override fun provideSummary(settingsItem: EditText): String? {
                return settingsItem.value
            }
        }

        init {
            value = AppGlobal.preferences.getString(key, defaultValue)
        }

        companion object {
            const val ItemType = 3
        }
    }

    data class CheckBox(
        val itemKey: String,
        val title: String? = null,
        val summary: String? = null,
        private val isChecked: Boolean? = null,
        private val defaultValue: Boolean? = null,
    ) : SettingsItem<Boolean>(itemKey) {

        override val id: Int = -1

        init {
            value = if (defaultValue == null) {
                isChecked
            } else {
                AppGlobal.preferences.getBoolean(key, defaultValue)
            }
        }

        companion object {
            const val ItemType = 4
        }
    }

    data class Switch(
        val itemKey: String,
        val title: String? = null,
        val summary: String? = null,
        private val isChecked: Boolean? = null,
        private val defaultValue: Boolean? = null,
    ) : SettingsItem<Boolean>(itemKey) {

        override val id: Int = -1

        init {
            value = if (defaultValue == null) {
                isChecked
            } else {
                AppGlobal.preferences.getBoolean(key, defaultValue)
            }
        }

        companion object {
            const val ItemType = 5
        }
    }
}
