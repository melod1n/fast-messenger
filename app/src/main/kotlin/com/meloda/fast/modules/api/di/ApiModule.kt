package com.meloda.fast.modules.api.di

import com.meloda.fast.data.audios.AudiosRepository
import com.meloda.fast.data.audios.AudiosService
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.data.files.FilesService
import com.meloda.fast.data.photos.PhotosRepository
import com.meloda.fast.data.photos.PhotosService
import com.meloda.fast.data.users.UsersService
import com.meloda.fast.data.users.data.UsersRepositoryImpl
import com.meloda.fast.data.users.data.UsersUseCaseImpl
import com.meloda.fast.data.users.domain.UsersRepository
import com.meloda.fast.data.users.domain.UsersUseCase
import com.meloda.fast.data.videos.VideosRepository
import com.meloda.fast.data.videos.VideosService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {
    single { get<Retrofit>().create(PhotosService::class.java) }
    single { get<Retrofit>().create(VideosService::class.java) }
    single { get<Retrofit>().create(AudiosService::class.java) }
    single { get<Retrofit>().create(FilesService::class.java) }

    single { get<Retrofit>().create(UsersService::class.java) }
    singleOf(::UsersRepositoryImpl) bind UsersRepository::class
    singleOf(::UsersUseCaseImpl) bind UsersUseCase::class

    singleOf(::PhotosRepository)
    singleOf(::VideosRepository)
    singleOf(::AudiosRepository)
    singleOf(::FilesRepository)
}
