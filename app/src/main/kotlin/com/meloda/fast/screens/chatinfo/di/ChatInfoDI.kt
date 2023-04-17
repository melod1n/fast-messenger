package com.meloda.fast.screens.chatinfo.di

import com.meloda.fast.screens.chatinfo.ChatInfoViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val chatInfoModule = module {
    viewModelOf(::ChatInfoViewModel)
}
