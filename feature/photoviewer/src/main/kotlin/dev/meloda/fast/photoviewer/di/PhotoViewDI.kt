package dev.meloda.fast.photoviewer.di

import dev.meloda.fast.photoviewer.PhotoViewViewModel
import dev.meloda.fast.photoviewer.PhotoViewViewModelImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val photoViewModule = module {
    viewModel {
        PhotoViewViewModelImpl(
            savedStateHandle = get(),
            applicationContext = get()
        )
    } bind PhotoViewViewModel::class
}
