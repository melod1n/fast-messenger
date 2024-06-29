package com.meloda.app.fast.auth.twofa.di

import com.meloda.app.fast.auth.twofa.AuthUseCase
import com.meloda.app.fast.auth.twofa.AuthUseCaseImpl
import com.meloda.app.fast.auth.twofa.TwoFaViewModel
import com.meloda.app.fast.auth.twofa.TwoFaViewModelImpl
import com.meloda.app.fast.auth.twofa.validation.TwoFaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val twoFaModule = module {
    singleOf(::TwoFaValidator)
    viewModelOf(::TwoFaViewModelImpl) bind TwoFaViewModel::class
    singleOf(::AuthUseCaseImpl) bind AuthUseCase::class
}
