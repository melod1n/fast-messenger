package com.meloda.app.fast.profile.di

import com.meloda.app.fast.profile.ProfileViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    viewModelOf(::ProfileViewModelImpl)
}
