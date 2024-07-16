package dev.meloda.fast.photoviewer.di

import dev.meloda.fast.photoviewer.PhotoViewViewModel
import dev.meloda.fast.photoviewer.PhotoViewViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val photoViewModule = module {
    viewModelOf(::PhotoViewViewModelImpl) bind PhotoViewViewModel::class
}
