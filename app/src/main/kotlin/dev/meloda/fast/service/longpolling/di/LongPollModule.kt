package dev.meloda.fast.service.longpolling.di

import dev.meloda.fast.domain.LongPollUpdatesParser
import dev.meloda.fast.domain.LongPollUseCase
import dev.meloda.fast.domain.LongPollUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val longPollModule = module {
    singleOf(::LongPollUseCaseImpl) bind LongPollUseCase::class
    singleOf(::LongPollUpdatesParser)
}
