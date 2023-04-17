package com.meloda.fast.screens.twofa.di

import com.meloda.fast.di.navigationModule
import com.meloda.fast.screens.twofa.presentation.TwoFaViewModelImpl
import com.meloda.fast.screens.twofa.screen.TwoFaCoordinator
import com.meloda.fast.screens.twofa.screen.TwoFaCoordinatorImpl
import com.meloda.fast.screens.twofa.screen.TwoFaScreen
import com.meloda.fast.screens.twofa.validation.TwoFaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

val twoFaModule = module {
    val moduleQualifier = named("twoFa")

    includes(navigationModule)

    single(moduleQualifier) { screen().resultFlow }
    single { screen().retrieveArguments() }

    single {
        TwoFaCoordinatorImpl(
            resultFlow = get(moduleQualifier),
            router = get()
        )
    } bind TwoFaCoordinator::class

    singleOf(::TwoFaValidator)
    viewModelOf(::TwoFaViewModelImpl)
}

private fun Scope.screen(): TwoFaScreen = get()
