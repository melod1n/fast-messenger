package com.meloda.fast.screens.messages.di

import com.meloda.fast.screens.messages.MessagesHistoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val messagesHistoryModule = module {
    viewModelOf(::MessagesHistoryViewModel)
}
