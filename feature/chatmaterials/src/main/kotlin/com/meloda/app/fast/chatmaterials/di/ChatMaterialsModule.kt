package com.meloda.app.fast.chatmaterials.di

import com.meloda.app.fast.chatmaterials.ChatMaterialsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val chatMaterialsModule = module {
    viewModelOf(::ChatMaterialsViewModelImpl)
}
