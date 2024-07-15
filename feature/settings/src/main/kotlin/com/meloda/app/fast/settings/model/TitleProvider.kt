package com.meloda.app.fast.settings.model

import com.meloda.app.fast.common.UiText

fun interface TitleProvider<T, S : SettingsItem<T>> {
    fun provideTitle(item: S): UiText?
}
