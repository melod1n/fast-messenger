package com.meloda.fast.screens.updates.di

import com.meloda.fast.screens.updates.UpdatesViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val updatesModule = module {
    viewModelOf(::UpdatesViewModelImpl)
}
