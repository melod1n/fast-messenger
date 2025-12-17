package dev.meloda.fast.convos.di

import dev.meloda.fast.convos.CreateChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val createChatModule = module {
    viewModelOf(::CreateChatViewModel)
}
