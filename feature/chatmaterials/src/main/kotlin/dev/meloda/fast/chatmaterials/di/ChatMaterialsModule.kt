package dev.meloda.fast.chatmaterials.di

import dev.meloda.fast.chatmaterials.ChatMaterialsViewModelImpl
import dev.meloda.fast.chatmaterials.model.MaterialType
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val chatMaterialsModule = module {
    viewModel(named(MaterialType.PHOTO)) {
        ChatMaterialsViewModelImpl(
            materialType = MaterialType.PHOTO,
            messagesUseCase = get(),
            savedStateHandle = get()
        )
    }
    viewModel(named(MaterialType.AUDIO)) {
        ChatMaterialsViewModelImpl(
            materialType = MaterialType.AUDIO,
            messagesUseCase = get(),
            savedStateHandle = get()
        )
    }
    viewModel(named(MaterialType.VIDEO)) {
        ChatMaterialsViewModelImpl(
            materialType = MaterialType.VIDEO,
            messagesUseCase = get(),
            savedStateHandle = get()
        )
    }
    viewModel(named(MaterialType.FILE)) {
        ChatMaterialsViewModelImpl(
            materialType = MaterialType.FILE,
            messagesUseCase = get(),
            savedStateHandle = get()
        )
    }
    viewModel(named(MaterialType.LINK)) {
        ChatMaterialsViewModelImpl(
            materialType = MaterialType.LINK,
            messagesUseCase = get(),
            savedStateHandle = get()
        )
    }
}
