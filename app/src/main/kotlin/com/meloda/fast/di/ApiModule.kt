package com.meloda.fast.di

import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.data.account.data.service.AccountService
import com.meloda.fast.data.audios.AudiosApi
import com.meloda.fast.data.files.FilesApi
import com.meloda.fast.data.photos.PhotosService
import com.meloda.fast.data.users.UsersApi
import com.meloda.fast.data.videos.VideosApi
import com.meloda.fast.screens.conversations.data.service.ConversationsService
import com.meloda.fast.screens.messages.data.service.MessagesService
import com.meloda.fast.service.longpolling.data.service.LongPollService
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {
    single { api(ConversationsService::class.java) }
    single { api(UsersApi::class.java) }
    single { api(MessagesService::class.java) }
    single { api(LongPollService::class.java) }
    single { api(AccountService::class.java) }
    single { api(PhotosService::class.java) }
    single { api(VideosApi::class.java) }
    single { api(AudiosApi::class.java) }
    single { api(FilesApi::class.java) }

    singleOf(::LongPollUpdatesParser)
}

internal fun <T> Scope.api(className: Class<T>): T = get<Retrofit>().create(className)
