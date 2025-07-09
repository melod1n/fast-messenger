package dev.meloda.fast.auth.login.di

import dev.meloda.fast.auth.login.LoginViewModel
import dev.meloda.fast.auth.login.validation.LoginValidator
import dev.meloda.fast.domain.OAuthUseCase
import dev.meloda.fast.domain.OAuthUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val loginModule = module {
    singleOf(::LoginValidator)
    viewModelOf(::LoginViewModel)
    singleOf(::OAuthUseCaseImpl) bind OAuthUseCase::class
}
