package com.meloda.fast.auth.login.di

import com.meloda.fast.auth.login.LoginViewModel
import com.meloda.fast.auth.login.LoginViewModelImpl
import com.meloda.fast.auth.login.OAuthUseCase
import com.meloda.fast.auth.login.OAuthUseCaseImpl
import com.meloda.fast.auth.login.validation.LoginValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val loginModule = module {
    singleOf(::LoginValidator)
    viewModelOf(::LoginViewModelImpl) bind LoginViewModel::class
    singleOf(::OAuthUseCaseImpl) bind OAuthUseCase::class
}
