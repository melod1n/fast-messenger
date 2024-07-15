package com.meloda.app.fast.settings.model

import android.content.res.Resources
import androidx.compose.runtime.Immutable
import com.meloda.app.fast.common.model.UiText
import com.meloda.app.fast.common.model.parseString
import com.meloda.app.fast.datastore.AppSettings
import kotlin.reflect.KClass

@Immutable
sealed class SettingsItem<T>(
    val key: String,
    value: T,
    defaultValue: T?
) {
    private val haveValue
        get() = this::class !in listOf<KClass<*>>(
            Title::class,
            TitleText::class
        )

    init {
        require(key.trim().isNotEmpty()) {
            "Key must not be empty"
        }

        require(!haveValue || defaultValue != null) {
            "Default value must not be null"
        }
    }

    var isVisible: Boolean = true

    var isEnabled: Boolean = true

    var value: T = value
        protected set(newValue) {
            field = newValue

            AppSettings.put(key, newValue)
        }

    var title: UiText? = null

    var text: UiText? = null

    var textProvider: TextProvider<T, SettingsItem<T>>? = null
        set(value) {
            field = value
            updateText()
        }

    fun updateText() {
        textProvider?.provideText(this)?.let { newText -> text = newText }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateValue(newValue: Any?) {
        if (!haveValue) throw IllegalStateException("This item does not have a value")
        value = newValue as T
    }

    class Title(
        key: String,
        title: UiText,
        isVisible: Boolean = true
    ) : SettingsItem<Unit>(
        key = key,
        value = Unit,
        defaultValue = null
    ) {

        init {
            this.title = title
            this.isVisible = isVisible
        }
    }

    class TitleText(
        key: String,
        title: UiText? = null,
        text: UiText? = null,
        isVisible: Boolean = true,
        isEnabled: Boolean = true
    ) : SettingsItem<Unit>(
        key = key,
        value = Unit,
        defaultValue = null
    ) {

        init {
            require(title != null || text != null) {
                "Either title or text must not be null"
            }

            this.title = title
            this.text = textProvider?.provideText(this) ?: text
            this.isVisible = isVisible
            this.isEnabled = isEnabled
        }
    }

    class Switch(
        key: String,
        defaultValue: Boolean,
        title: UiText? = null,
        text: UiText? = null,
        isVisible: Boolean = true,
        isEnabled: Boolean = true,
        isChecked: Boolean? = null
    ) : SettingsItem<Boolean>(
        key = key,
        value = isChecked ?: getCurrentValue(key, defaultValue),
        defaultValue = defaultValue
    ) {

        init {
            require(title != null || text != null) {
                "Either title or text must not be null"
            }

            this.title = title
            this.text = textProvider?.provideText(this) ?: text
            this.isVisible = isVisible
            this.isEnabled = isEnabled
        }
    }

    class TextField(
        key: String,
        defaultValue: String,
        title: UiText? = null,
        text: UiText? = null,
        isVisible: Boolean = true,
        isEnabled: Boolean = true,
        fieldText: String? = null
    ) : SettingsItem<String>(
        key = key,
        value = fieldText ?: getCurrentValue(key, defaultValue),
        defaultValue = defaultValue
    ) {

        init {
            require(title != null || text != null) {
                "Either title or text must not be null"
            }

            this.title = title
            this.text = textProvider?.provideText(this) ?: text
            this.isVisible = isVisible
            this.isEnabled = isEnabled
        }
    }

    class ListItem<T : Any>(
        key: String,
        defaultValue: T,
        valueClass: KClass<T>,
        title: UiText? = null,
        text: UiText? = null,
        isVisible: Boolean = true,
        isEnabled: Boolean = true,
        selectedValue: T? = null,
        val titles: List<UiText>,
        val values: List<T>
    ) : SettingsItem<T>(
        key = key,
        value = selectedValue ?: AppSettings.get(valueClass, key, defaultValue),
        defaultValue = defaultValue
    ) {

        init {
            require(title != null || text != null) {
                "Either title or text must not be null"
            }

            require(titles.isNotEmpty()) {
                "titles must not be empty"
            }

            this.title = title
            this.text = textProvider?.provideText(this) ?: text
            this.isVisible = isVisible
            this.isEnabled = isEnabled
        }
    }

    fun asPresentation(resources: Resources): UiItem = when (val item = this) {
        is Title -> {
            UiItem.Title(
                key = item.key,
                title = item.title.parseString(resources).orEmpty(),
                isVisible = item.isVisible
            )
        }

        is TitleText -> {
            UiItem.TitleText(
                key = item.key,
                title = item.title.parseString(resources),
                text = item.text.parseString(resources),
                isVisible = item.isVisible,
                isEnabled = item.isEnabled
            )
        }

        is Switch -> {
            UiItem.Switch(
                key = item.key,
                title = item.title.parseString(resources),
                text = item.text.parseString(resources),
                isVisible = item.isVisible,
                isEnabled = item.isEnabled,
                isChecked = item.value
            )
        }

        is TextField -> {
            UiItem.TextField(
                key = item.key,
                title = item.title.parseString(resources),
                text = item.text.parseString(resources),
                isVisible = item.isVisible,
                isEnabled = item.isEnabled,
                fieldText = item.value
            )
        }

        is ListItem<*> -> {
            UiItem.List(
                key = item.key,
                title = item.title.parseString(resources),
                text = item.text.parseString(resources),
                isVisible = item.isVisible,
                isEnabled = item.isEnabled,
                selectedValue = item.value,
                titles = item.titles.mapNotNull { it.parseString(resources) },
                values = item.values
            )
        }
    }
}

private inline fun <reified T> getCurrentValue(key: String, defaultValue: T): T {
    if (T::class == Nothing::class) {
        throw IllegalStateException("Items with Nothing does not have a value")
    } else {
        return AppSettings.get(key, defaultValue)
    }
}
