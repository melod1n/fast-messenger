package com.meloda.fast.screens.messages.di

import com.meloda.fast.data.messages.data.repository.MessagesRepositoryImpl
import com.meloda.fast.data.messages.data.usecase.MessagesUseCaseImpl
import com.meloda.fast.data.messages.domain.repository.MessagesRepository
import com.meloda.fast.data.messages.domain.usecase.MessagesUseCase
import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.validation.MessagesHistoryValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val messagesHistoryModule = module {
    singleOf(::MessagesRepositoryImpl) bind MessagesRepository::class
    singleOf(::MessagesUseCaseImpl) bind MessagesUseCase::class
    singleOf(::MessagesHistoryValidator)
    viewModelOf(::MessagesHistoryViewModelImpl)
}
