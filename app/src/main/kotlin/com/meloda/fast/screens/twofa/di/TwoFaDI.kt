package com.meloda.fast.screens.twofa.di

import com.meloda.fast.di.navigationModule
import com.meloda.fast.screens.twofa.TwoFaViewModelImpl
import com.meloda.fast.screens.twofa.screen.TwoFaScreen
import com.meloda.fast.screens.twofa.validation.TwoFaValidator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val twoFaModule = module {
    includes(navigationModule)

    single { TwoFaValidator() }
    single(named("twoFaResultFlow")) { get<TwoFaScreen>().resultFlow }

    viewModel {
        TwoFaViewModelImpl(
            resultFlow = get(named("twoFaResultFlow")),
            router = get(),
            validator = get()
        )
    }
}
