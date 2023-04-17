package com.meloda.fast.screens.login.di

import com.meloda.fast.screens.login.LoginViewModelImpl
import com.meloda.fast.screens.login.validation.LoginValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    single { LoginValidator() }
    viewModelOf(::LoginViewModelImpl)
}
