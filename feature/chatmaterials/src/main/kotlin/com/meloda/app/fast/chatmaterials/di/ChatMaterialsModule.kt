package dev.meloda.fast.chatmaterials.di

import dev.meloda.fast.chatmaterials.ChatMaterialsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val chatMaterialsModule = module {
    viewModelOf(::ChatMaterialsViewModelImpl)
}
