package com.meloda.fast.screens.messages.di

import com.meloda.fast.screens.messages.data.repository.MessagesRepositoryImpl
import com.meloda.fast.screens.messages.data.usecase.MessagesUseCaseImpl
import com.meloda.fast.screens.messages.domain.repository.MessagesRepository
import com.meloda.fast.screens.messages.domain.usecase.MessagesUseCase
import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.data.service.MessagesService
import com.meloda.fast.screens.messages.validation.MessagesHistoryValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val messagesHistoryModule = module {
    single { get<Retrofit>().create(MessagesService::class.java) }
    singleOf(::MessagesRepositoryImpl) bind MessagesRepository::class
    singleOf(::MessagesUseCaseImpl) bind MessagesUseCase::class
    singleOf(::MessagesHistoryValidator)
    viewModelOf(::MessagesHistoryViewModelImpl)
}
