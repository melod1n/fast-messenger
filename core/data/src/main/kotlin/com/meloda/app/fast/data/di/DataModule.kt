package com.meloda.app.fast.data.di

import com.meloda.app.fast.common.di.commonModule
import com.meloda.app.fast.data.api.account.AccountRepository
import com.meloda.app.fast.data.api.account.AccountRepositoryImpl
import com.meloda.app.fast.data.api.account.AccountUseCase
import com.meloda.app.fast.data.api.account.AccountUseCaseImpl
import com.meloda.app.fast.data.api.audios.AudiosRepository
import com.meloda.app.fast.data.api.auth.AuthRepository
import com.meloda.app.fast.data.api.auth.AuthRepositoryImpl
import com.meloda.app.fast.data.api.conversations.ConversationsRepository
import com.meloda.app.fast.data.api.conversations.ConversationsRepositoryImpl
import com.meloda.app.fast.data.api.files.FilesRepository
import com.meloda.app.fast.data.api.friends.FriendsRepository
import com.meloda.app.fast.data.api.friends.FriendsRepositoryImpl
import com.meloda.app.fast.data.api.longpoll.LongPollRepository
import com.meloda.app.fast.data.api.longpoll.LongPollRepositoryImpl
import com.meloda.app.fast.data.api.messages.MessagesRepository
import com.meloda.app.fast.data.api.messages.MessagesRepositoryImpl
import com.meloda.app.fast.data.api.oauth.OAuthRepository
import com.meloda.app.fast.data.api.oauth.OAuthRepositoryImpl
import com.meloda.app.fast.data.api.photos.PhotosRepository
import com.meloda.app.fast.data.api.users.UsersRepository
import com.meloda.app.fast.data.api.users.UsersRepositoryImpl
import com.meloda.app.fast.data.api.users.UsersUseCase
import com.meloda.app.fast.data.api.users.UsersUseCaseImpl
import com.meloda.app.fast.data.api.videos.VideosRepository
import com.meloda.app.fast.data.db.AccountsRepository
import com.meloda.app.fast.data.db.AccountsRepositoryImpl
import com.meloda.app.fast.database.di.databaseModule
import com.meloda.app.fast.datastore.di.dataStoreModule
import com.meloda.app.fast.network.di.networkModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    includes(
        commonModule,
        databaseModule,
        dataStoreModule,
        networkModule,
    )

    singleOf(::AccountRepositoryImpl) bind AccountRepository::class
    singleOf(::AccountUseCaseImpl) bind AccountUseCase::class

    singleOf(::AudiosRepository)

    singleOf(::AuthRepositoryImpl) bind AuthRepository::class

    singleOf(::ConversationsRepositoryImpl) bind ConversationsRepository::class

    singleOf(::FilesRepository)

    singleOf(::LongPollRepositoryImpl) bind LongPollRepository::class

    singleOf(::MessagesRepositoryImpl) bind MessagesRepository::class

    singleOf(::OAuthRepositoryImpl) bind OAuthRepository::class

    singleOf(::PhotosRepository)

    singleOf(::UsersRepositoryImpl) bind UsersRepository::class
    singleOf(::UsersUseCaseImpl) bind UsersUseCase::class

    singleOf(::VideosRepository)

    singleOf(::AccountsRepositoryImpl) bind AccountsRepository::class

    singleOf(::FriendsRepositoryImpl) bind FriendsRepository::class
}
