package com.meloda.app.fast.service.longpolling.di

import com.meloda.app.fast.service.longpolling.LongPollUpdatesParser
import com.meloda.app.fast.service.longpolling.LongPollUseCase
import com.meloda.app.fast.service.longpolling.LongPollUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val longPollModule = module {
    singleOf(::LongPollUseCaseImpl) bind LongPollUseCase::class

    singleOf(::LongPollUpdatesParser)
}
