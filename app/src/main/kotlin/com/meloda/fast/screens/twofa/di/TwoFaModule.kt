package com.meloda.fast.screens.twofa.di

import com.meloda.fast.di.navigationModule
import com.meloda.fast.screens.twofa.presentation.TwoFaViewModelImpl
import com.meloda.fast.screens.twofa.validation.TwoFaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val twoFaModule = module {
    includes(navigationModule)

    singleOf(::TwoFaValidator)
    viewModelOf(::TwoFaViewModelImpl)
}
