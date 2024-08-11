package dev.meloda.fast.messageshistory.di

import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.messageshistory.MessagesHistoryViewModel
import dev.meloda.fast.messageshistory.MessagesHistoryViewModelImpl
import dev.meloda.fast.domain.MessagesUseCaseImpl
import dev.meloda.fast.messageshistory.validation.MessagesHistoryValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val messagesHistoryModule = module {
    singleOf(::MessagesUseCaseImpl) bind dev.meloda.fast.domain.MessagesUseCase::class
    singleOf(::MessagesHistoryValidator)
    viewModelOf(::MessagesHistoryViewModelImpl) bind MessagesHistoryViewModel::class
}
