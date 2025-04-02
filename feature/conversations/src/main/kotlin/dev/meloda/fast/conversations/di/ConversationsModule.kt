package dev.meloda.fast.conversations.di

import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.domain.ConversationsUseCase
import dev.meloda.fast.domain.ConversationsUseCaseImpl
import dev.meloda.fast.model.ConversationFilter
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val conversationsModule = module {
    singleOf(::ConversationsUseCaseImpl) bind ConversationsUseCase::class
    viewModelOf(::ConversationsViewModelImpl)

    viewModel(named(ConversationFilter.ALL)) {
        ConversationsViewModelImpl(
            filter = ConversationFilter.ALL,
            updatesParser = get(),
            conversationsUseCase = get(),
            messagesUseCase = get(),
            resources = get(),
            userSettings = get(),
            imageLoader = get(),
            applicationContext = get(),
            loadConversationsByIdUseCase = get()
        )
    }
}
