package dev.meloda.fast.conversations.di

import dev.meloda.fast.conversations.CreateChatViewModelImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val createChatModule = module {
    viewModelOf(::CreateChatViewModelImpl)
}
