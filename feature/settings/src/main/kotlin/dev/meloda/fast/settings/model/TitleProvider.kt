package dev.meloda.fast.settings.model

import dev.meloda.fast.common.model.UiText

fun interface TitleProvider<T, S : SettingsItem<T>> {
    fun provideTitle(item: S): UiText?
}
