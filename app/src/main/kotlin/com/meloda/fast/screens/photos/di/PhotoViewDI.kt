package com.meloda.fast.screens.photos.di

import com.meloda.fast.screens.photos.PhotoViewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val photoViewModule = module {
    viewModelOf(::PhotoViewViewModel)
}
