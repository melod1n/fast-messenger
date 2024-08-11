package dev.meloda.fast.domain.di

import dev.meloda.fast.data.di.dataModule
import dev.meloda.fast.domain.AccountUseCase
import dev.meloda.fast.domain.AccountUseCaseImpl
import dev.meloda.fast.domain.GetCurrentAccountUseCase
import dev.meloda.fast.domain.UsersUseCase
import dev.meloda.fast.domain.UsersUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    singleOf(::UsersUseCaseImpl) bind UsersUseCase::class
    singleOf(::AccountUseCaseImpl) bind AccountUseCase::class
    singleOf(::GetCurrentAccountUseCase)
}
