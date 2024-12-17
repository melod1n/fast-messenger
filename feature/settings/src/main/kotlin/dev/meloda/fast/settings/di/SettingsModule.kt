package dev.meloda.fast.settings.di

import dev.meloda.fast.settings.SettingsViewModel
import dev.meloda.fast.settings.SettingsViewModelImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModelImpl) bind SettingsViewModel::class
}
