package dev.meloda.fast.domain.di

import dev.meloda.fast.data.di.dataModule
import dev.meloda.fast.domain.AccountUseCase
import dev.meloda.fast.domain.AccountUseCaseImpl
import dev.meloda.fast.domain.GetCurrentAccountUseCase
import dev.meloda.fast.domain.GetLocalUserByIdUseCase
import dev.meloda.fast.domain.GetLocalUsersByIdsUseCase
import dev.meloda.fast.domain.GetMessageReadPeersUseCase
import dev.meloda.fast.domain.LoadConvosByIdUseCase
import dev.meloda.fast.domain.LoadUserByIdUseCase
import dev.meloda.fast.domain.LoadUsersByIdsUseCase
import dev.meloda.fast.domain.StoreUsersUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    singleOf(::GetLocalUserByIdUseCase)
    singleOf(::GetLocalUsersByIdsUseCase)
    singleOf(::LoadUserByIdUseCase)
    singleOf(::LoadUsersByIdsUseCase)
    singleOf(::StoreUsersUseCase)

    singleOf(::AccountUseCaseImpl) bind AccountUseCase::class
    singleOf(::GetCurrentAccountUseCase)

    singleOf(::LoadConvosByIdUseCase)

    singleOf(::GetMessageReadPeersUseCase)
}
