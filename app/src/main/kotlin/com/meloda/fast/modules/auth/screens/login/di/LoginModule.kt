package com.meloda.fast.modules.auth.screens.login.di

import com.meloda.fast.modules.auth.screens.login.LoginViewModelImpl
import com.meloda.fast.modules.auth.screens.login.validation.LoginValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    single { LoginValidator() }
    viewModelOf(::LoginViewModelImpl)
}
