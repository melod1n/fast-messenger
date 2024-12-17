package dev.meloda.fast.conversations.di

import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.domain.ConversationsUseCase
import dev.meloda.fast.domain.ConversationsUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val conversationsModule = module {
    singleOf(::ConversationsUseCaseImpl) bind ConversationsUseCase::class
    viewModelOf(::ConversationsViewModelImpl)
}
