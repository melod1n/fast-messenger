package com.meloda.fast.di

import com.meloda.fast.data.audios.AudiosApi
import com.meloda.fast.data.files.FilesApi
import com.meloda.fast.data.photos.PhotosService
import com.meloda.fast.data.users.UsersApi
import com.meloda.fast.data.videos.VideosApi
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {
    single { api(UsersApi::class.java) }
    single { api(PhotosService::class.java) }
    single { api(VideosApi::class.java) }
    single { api(AudiosApi::class.java) }
    single { api(FilesApi::class.java) }
}

internal fun <T> Scope.api(className: Class<T>): T = get<Retrofit>().create(className)
