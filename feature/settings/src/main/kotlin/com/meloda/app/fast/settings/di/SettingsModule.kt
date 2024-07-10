package com.meloda.app.fast.settings.di

import com.meloda.app.fast.settings.SettingsViewModel
import com.meloda.app.fast.settings.SettingsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModelImpl) bind SettingsViewModel::class
}
