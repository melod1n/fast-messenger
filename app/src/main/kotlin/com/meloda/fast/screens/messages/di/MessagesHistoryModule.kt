package com.meloda.fast.screens.messages.di

import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.validation.MessagesHistoryValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val messagesHistoryModule = module {
    singleOf(::MessagesHistoryValidator)
    viewModelOf(::MessagesHistoryViewModelImpl)
}
