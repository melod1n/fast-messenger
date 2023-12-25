package com.meloda.fast.screens.main.di

import com.meloda.fast.screens.main.MainViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val mainModule = module {
    viewModelOf(::MainViewModelImpl) {
        qualifier = qualifier("main")
    }
}
