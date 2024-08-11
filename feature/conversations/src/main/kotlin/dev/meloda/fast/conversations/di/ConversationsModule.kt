package dev.meloda.fast.conversations.di

import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.domain.ConversationsUseCaseImpl
import dev.meloda.fast.domain.ConversationsUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val conversationsModule = module {
    singleOf(::ConversationsUseCaseImpl) bind dev.meloda.fast.domain.ConversationsUseCase::class
    viewModelOf(::ConversationsViewModelImpl)
}
