package dev.meloda.fast.auth.login.di

import dev.meloda.fast.auth.login.LoginViewModelImpl
import dev.meloda.fast.domain.OAuthUseCase
import dev.meloda.fast.domain.OAuthUseCaseImpl
import dev.meloda.fast.auth.login.validation.LoginValidator
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val loginModule = module {
    singleOf(::LoginValidator)
    viewModelOf(::LoginViewModelImpl) bind dev.meloda.fast.auth.login.LoginViewModel::class
    singleOf(::OAuthUseCaseImpl) bind OAuthUseCase::class
}
