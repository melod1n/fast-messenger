package com.meloda.app.fast.auth.screens.twofa.di

import com.meloda.app.fast.auth.screens.twofa.TwoFaViewModel
import com.meloda.app.fast.auth.screens.twofa.TwoFaViewModelImpl
import com.meloda.app.fast.auth.screens.twofa.validation.TwoFaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val twoFaModule = module {
    singleOf(::TwoFaValidator)
    viewModelOf(::TwoFaViewModelImpl) bind TwoFaViewModel::class
}
