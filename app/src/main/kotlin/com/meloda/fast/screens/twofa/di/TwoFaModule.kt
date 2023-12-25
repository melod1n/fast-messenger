package com.meloda.fast.screens.twofa.di

import com.meloda.fast.screens.twofa.TwoFaViewModelImpl
import com.meloda.fast.screens.twofa.validation.TwoFaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val twoFaModule = module {
    singleOf(::TwoFaValidator)
    viewModelOf(::TwoFaViewModelImpl)
}
