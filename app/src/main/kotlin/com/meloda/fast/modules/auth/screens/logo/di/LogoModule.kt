package com.meloda.fast.modules.auth.screens.logo.di

import com.meloda.fast.modules.auth.screens.logo.LogoViewModel
import com.meloda.fast.modules.auth.screens.logo.LogoViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val logoModule = module {
    viewModelOf(::LogoViewModelImpl) bind LogoViewModel::class
}
