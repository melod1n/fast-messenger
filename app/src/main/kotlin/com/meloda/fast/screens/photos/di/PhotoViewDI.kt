package com.meloda.fast.screens.photos.di

import com.meloda.fast.screens.photos.PhotoViewViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val photoViewModule = module {
    viewModelOf(::PhotoViewViewModelImpl)
}
