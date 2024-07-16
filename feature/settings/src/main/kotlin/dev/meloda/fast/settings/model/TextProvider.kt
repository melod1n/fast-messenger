package dev.meloda.fast.settings.model

import dev.meloda.fast.common.model.UiText

fun interface TextProvider<T, S : SettingsItem<T>> {
    fun provideText(item: S): UiText?
}
