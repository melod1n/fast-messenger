package com.meloda.app.fast.auth.screens.login.di

import com.meloda.app.fast.auth.screens.login.LoginViewModel
import com.meloda.app.fast.auth.screens.login.LoginViewModelImpl
import com.meloda.app.fast.auth.screens.login.validation.LoginValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val loginModule = module {
    singleOf(::LoginValidator)
    viewModelOf(::LoginViewModelImpl) bind LoginViewModel::class
}
