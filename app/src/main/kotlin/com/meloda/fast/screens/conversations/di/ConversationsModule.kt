package com.meloda.fast.screens.conversations.di

import com.meloda.fast.screens.conversations.ConversationsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val conversationsModule = module {
    viewModelOf(::ConversationsViewModelImpl)
}
