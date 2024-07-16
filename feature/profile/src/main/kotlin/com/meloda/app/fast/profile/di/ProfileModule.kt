package dev.meloda.fast.profile.di

import dev.meloda.fast.profile.ProfileViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    viewModelOf(::ProfileViewModelImpl)
}
