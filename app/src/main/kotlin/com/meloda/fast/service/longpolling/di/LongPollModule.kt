package com.meloda.fast.service.longpolling.di

import com.meloda.fast.service.longpolling.LongPollUpdatesParser
import com.meloda.fast.service.longpolling.data.repository.LongPollRepositoryImpl
import com.meloda.fast.service.longpolling.data.service.LongPollService
import com.meloda.fast.service.longpolling.data.usecase.LongPollUseCaseImpl
import com.meloda.fast.service.longpolling.domain.repository.LongPollRepository
import com.meloda.fast.service.longpolling.domain.usecase.LongPollUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val longPollModule = module {
    single { get<Retrofit>().create(LongPollService::class.java) }
    singleOf(::LongPollRepositoryImpl) bind LongPollRepository::class
    singleOf(::LongPollUseCaseImpl) bind LongPollUseCase::class

    singleOf(::LongPollUpdatesParser)
}
