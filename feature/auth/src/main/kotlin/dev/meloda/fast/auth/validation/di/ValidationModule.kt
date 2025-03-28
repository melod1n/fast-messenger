package dev.meloda.fast.auth.validation.di

import dev.meloda.fast.domain.AuthUseCase
import dev.meloda.fast.domain.AuthUseCaseImpl
import dev.meloda.fast.auth.validation.ValidationViewModel
import dev.meloda.fast.auth.validation.ValidationViewModelImpl
import dev.meloda.fast.auth.validation.validation.ValidationValidator
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val validationModule = module {
    singleOf(::ValidationValidator)
    viewModelOf(::ValidationViewModelImpl) bind ValidationViewModel::class
    singleOf(::AuthUseCaseImpl) bind AuthUseCase::class
}
