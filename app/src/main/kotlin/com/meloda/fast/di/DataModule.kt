package com.meloda.fast.di

import com.meloda.fast.data.audios.AudiosRepository
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.data.photos.PhotosRepository
import com.meloda.fast.data.users.UsersRepository
import com.meloda.fast.data.videos.VideosRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// TODO: 17.04.2023, Danil Nikolaev: use specific repositories in local DI modules
val dataModule = module {
    singleOf(::UsersRepository)
    singleOf(::PhotosRepository)
    singleOf(::VideosRepository)
    singleOf(::AudiosRepository)
    singleOf(::FilesRepository)
}
