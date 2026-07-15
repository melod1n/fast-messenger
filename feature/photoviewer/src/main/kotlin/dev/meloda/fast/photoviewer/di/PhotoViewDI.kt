package dev.meloda.fast.photoviewer.di

import dev.meloda.fast.photoviewer.PhotoViewViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val photoViewModule = module {
    viewModelOf(::PhotoViewViewModel)
}
