package com.meloda.app.fast.settings.model

import com.meloda.app.fast.common.model.UiText

fun interface TextProvider<T, S : SettingsItem<T>> {
    fun provideText(item: S): UiText?
}
