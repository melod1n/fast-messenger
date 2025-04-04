package dev.meloda.fast.conversations.di

import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.domain.ConversationsUseCase
import dev.meloda.fast.domain.ConversationsUseCaseImpl
import dev.meloda.fast.model.ConversationsFilter
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

val conversationsModule = module {
    viewModel(named(ConversationsFilter.ALL)) {
        createConversationsViewModel(ConversationsFilter.ALL)
    }
    viewModel(named(ConversationsFilter.ARCHIVE)) {
        createConversationsViewModel(ConversationsFilter.ARCHIVE)
    }

    singleOf(::ConversationsUseCaseImpl) bind ConversationsUseCase::class
}

private fun Scope.createConversationsViewModel(filter: ConversationsFilter): ConversationsViewModelImpl {
    return ConversationsViewModelImpl(
        filter = filter,
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
