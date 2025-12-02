package dev.meloda.fast.conversations.di

import dev.meloda.fast.conversations.CreateChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val createChatModule = module {
    viewModelOf(::CreateChatViewModel)
}
