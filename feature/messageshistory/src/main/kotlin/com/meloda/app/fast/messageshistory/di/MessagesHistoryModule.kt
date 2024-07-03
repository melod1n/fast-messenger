package com.meloda.app.fast.messageshistory.di

import com.meloda.app.fast.data.api.messages.MessagesUseCase
import com.meloda.app.fast.messageshistory.MessagesHistoryViewModel
import com.meloda.app.fast.messageshistory.MessagesHistoryViewModelImpl
import com.meloda.app.fast.messageshistory.domain.MessagesUseCaseImpl
import com.meloda.app.fast.messageshistory.validation.MessagesHistoryValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val messagesHistoryModule = module {
    singleOf(::MessagesUseCaseImpl) bind MessagesUseCase::class
    singleOf(::MessagesHistoryValidator)
    viewModelOf(::MessagesHistoryViewModelImpl) bind MessagesHistoryViewModel::class
}
