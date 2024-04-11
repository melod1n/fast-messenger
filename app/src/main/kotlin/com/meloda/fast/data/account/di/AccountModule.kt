package com.meloda.fast.data.account.di

import com.meloda.fast.data.account.data.repository.AccountRepositoryImpl
import com.meloda.fast.data.account.data.service.AccountService
import com.meloda.fast.data.account.data.usecase.AccountUseCaseImpl
import com.meloda.fast.data.account.domain.repository.AccountRepository
import com.meloda.fast.data.account.domain.usecase.AccountUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val accountModule = module {
    single { get<Retrofit>().create(AccountService::class.java) }
    singleOf(::AccountRepositoryImpl) bind AccountRepository::class
    singleOf(::AccountUseCaseImpl) bind AccountUseCase::class
}
