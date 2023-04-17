package com.meloda.fast.screens.settings.di

import com.meloda.fast.screens.settings.SettingsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModelImpl)
}
