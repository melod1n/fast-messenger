package dev.meloda.fast.conversations.di

import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.conversations.data.ConversationsUseCaseImpl
import dev.meloda.fast.data.api.conversations.ConversationsUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val conversationsModule = module {
    singleOf(::ConversationsUseCaseImpl) bind ConversationsUseCase::class

    viewModelOf(::ConversationsViewModelImpl)
}
