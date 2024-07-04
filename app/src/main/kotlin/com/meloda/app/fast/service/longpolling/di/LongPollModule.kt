package com.meloda.app.fast.service.longpolling.di

import com.meloda.app.fast.data.LongPollUpdatesParser
import com.meloda.app.fast.data.LongPollUseCase
import com.meloda.app.fast.data.LongPollUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val longPollModule = module {
    singleOf(::LongPollUseCaseImpl) bind LongPollUseCase::class
    singleOf(::LongPollUpdatesParser)
}
