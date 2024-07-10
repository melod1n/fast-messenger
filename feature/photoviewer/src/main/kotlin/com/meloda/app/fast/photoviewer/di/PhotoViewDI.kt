package com.meloda.app.fast.photoviewer.di

import com.meloda.app.fast.photoviewer.PhotoViewViewModel
import com.meloda.app.fast.photoviewer.PhotoViewViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val photoViewModule = module {
    viewModelOf(::PhotoViewViewModelImpl) bind PhotoViewViewModel::class
}
