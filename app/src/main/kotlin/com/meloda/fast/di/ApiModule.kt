package com.meloda.fast.di

import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.data.account.AccountApi
import com.meloda.fast.data.audios.AudiosApi
import com.meloda.fast.data.auth.AuthApi
import com.meloda.fast.screens.conversations.data.service.ConversationsService
import com.meloda.fast.data.files.FilesApi
import com.meloda.fast.data.longpoll.LongPollApi
import com.meloda.fast.data.messages.MessagesApi
import com.meloda.fast.data.ota.OtaApi
import com.meloda.fast.data.photos.PhotosApi
import com.meloda.fast.data.users.UsersApi
import com.meloda.fast.data.videos.VideosApi
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {
    single { api(AuthApi::class.java) }
    single { api(ConversationsService::class.java) }
    single { api(UsersApi::class.java) }
    single { api(MessagesApi::class.java) }
    single { api(LongPollApi::class.java) }
    single { api(AccountApi::class.java) }
    single { api(OtaApi::class.java) }
    single { api(PhotosApi::class.java) }
    single { api(VideosApi::class.java) }
    single { api(AudiosApi::class.java) }
    single { api(FilesApi::class.java) }

    singleOf(::LongPollUpdatesParser)
}

internal fun <T> Scope.api(className: Class<T>): T = get<Retrofit>().create(className)
