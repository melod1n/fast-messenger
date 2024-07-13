package com.meloda.app.fast.auth.validation.di

import com.meloda.app.fast.auth.validation.AuthUseCase
import com.meloda.app.fast.auth.validation.AuthUseCaseImpl
import com.meloda.app.fast.auth.validation.ValidationViewModel
import com.meloda.app.fast.auth.validation.ValidationViewModelImpl
import com.meloda.app.fast.auth.validation.validation.ValidationValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val validationModule = module {
    singleOf(::ValidationValidator)
    viewModelOf(::ValidationViewModelImpl) bind ValidationViewModel::class
    singleOf(::AuthUseCaseImpl) bind AuthUseCase::class
}
