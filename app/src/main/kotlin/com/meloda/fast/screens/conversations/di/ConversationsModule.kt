package com.meloda.fast.screens.conversations.di

import com.meloda.fast.screens.conversations.ConversationsViewModelImpl
import com.meloda.fast.screens.conversations.data.repository.ConversationsRepositoryImpl
import com.meloda.fast.screens.conversations.data.service.ConversationsService
import com.meloda.fast.screens.conversations.data.usecase.ConversationsUseCaseImpl
import com.meloda.fast.screens.conversations.domain.repository.ConversationsRepository
import com.meloda.fast.screens.conversations.domain.usecase.ConversationsUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val conversationsModule = module {
    single { get<Retrofit>().create(ConversationsService::class.java) }
    singleOf(::ConversationsRepositoryImpl) bind ConversationsRepository::class
    singleOf(::ConversationsUseCaseImpl) bind ConversationsUseCase::class

    viewModelOf(::ConversationsViewModelImpl)
}
