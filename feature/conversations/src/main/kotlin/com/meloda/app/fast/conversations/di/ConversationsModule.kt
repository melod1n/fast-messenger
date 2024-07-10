package com.meloda.app.fast.conversations.di

import com.meloda.app.fast.conversations.ConversationsViewModelImpl
import com.meloda.app.fast.conversations.data.ConversationsUseCaseImpl
import com.meloda.app.fast.data.api.conversations.ConversationsUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val conversationsModule = module {
    singleOf(::ConversationsUseCaseImpl) bind ConversationsUseCase::class

    viewModelOf(::ConversationsViewModelImpl)
}
