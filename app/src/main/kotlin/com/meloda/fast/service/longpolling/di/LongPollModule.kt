package com.meloda.fast.service.longpolling.di

import com.meloda.fast.service.longpolling.data.repository.LongPollRepositoryImpl
import com.meloda.fast.service.longpolling.data.usecase.LongPollUseCaseImpl
import com.meloda.fast.service.longpolling.domain.repository.LongPollRepository
import com.meloda.fast.service.longpolling.domain.usecase.LongPollUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val longPollModule = module {
    singleOf(::LongPollRepositoryImpl) bind LongPollRepository::class
    singleOf(::LongPollUseCaseImpl) bind LongPollUseCase::class
}
