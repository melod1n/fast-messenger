package com.meloda.fast.screens.settings.di

import com.meloda.fast.screens.settings.SettingsViewModelImpl
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.screens.settings.UserSettingsImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModelImpl)

    singleOf(::UserSettingsImpl) bind UserSettings::class
}
