package dev.meloda.fast.logger

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val loggerModule = module {
    singleOf(::FastLogger)
}
