package dev.meloda.fast.data.di

import dev.meloda.fast.data.AccessTokenInterceptor
import dev.meloda.fast.data.api.account.AccountRepository
import dev.meloda.fast.data.api.account.AccountRepositoryImpl
import dev.meloda.fast.data.api.audios.AudiosRepository
import dev.meloda.fast.data.api.auth.AuthRepository
import dev.meloda.fast.data.api.auth.AuthRepositoryImpl
import dev.meloda.fast.data.api.convos.ConvosRepository
import dev.meloda.fast.data.api.convos.ConvosRepositoryImpl
import dev.meloda.fast.data.api.files.FilesRepository
import dev.meloda.fast.data.api.friends.FriendsRepository
import dev.meloda.fast.data.api.friends.FriendsRepositoryImpl
import dev.meloda.fast.data.api.longpoll.LongPollRepository
import dev.meloda.fast.data.api.longpoll.LongPollRepositoryImpl
import dev.meloda.fast.data.api.messages.MessagesRepository
import dev.meloda.fast.data.api.messages.MessagesRepositoryImpl
import dev.meloda.fast.data.api.oauth.OAuthRepository
import dev.meloda.fast.data.api.oauth.OAuthRepositoryImpl
import dev.meloda.fast.data.api.photos.PhotosRepository
import dev.meloda.fast.data.api.users.UsersRepository
import dev.meloda.fast.data.api.users.UsersRepositoryImpl
import dev.meloda.fast.data.api.videos.VideosRepository
import dev.meloda.fast.data.db.AccountsRepository
import dev.meloda.fast.data.db.AccountsRepositoryImpl
import dev.meloda.fast.database.di.databaseModule
import dev.meloda.fast.datastore.di.dataStoreModule
import dev.meloda.fast.network.di.networkModule
import okhttp3.Interceptor
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    includes(
        databaseModule,
        dataStoreModule,
        networkModule,
    )

    singleOf(::AccountRepositoryImpl) bind AccountRepository::class

    singleOf(::AudiosRepository)

    singleOf(::AuthRepositoryImpl) bind AuthRepository::class

    singleOf(::ConvosRepositoryImpl) bind ConvosRepository::class

    singleOf(::FilesRepository)

    singleOf(::LongPollRepositoryImpl) bind LongPollRepository::class

    singleOf(::MessagesRepositoryImpl) bind MessagesRepository::class

    singleOf(::OAuthRepositoryImpl) bind OAuthRepository::class

    singleOf(::PhotosRepository)

    singleOf(::UsersRepositoryImpl) bind UsersRepository::class

    singleOf(::VideosRepository)

    singleOf(::AccountsRepositoryImpl) bind AccountsRepository::class

    singleOf(::FriendsRepositoryImpl) bind FriendsRepository::class

    single<Interceptor>(named("token_interceptor")) {
        AccessTokenInterceptor()
    }
}
