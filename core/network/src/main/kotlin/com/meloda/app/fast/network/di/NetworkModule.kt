package com.meloda.app.fast.network.di

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.meloda.app.fast.common.AppConstants
import com.meloda.app.fast.common.AuthInterceptor
import com.meloda.app.fast.network.JsonConverter
import com.meloda.app.fast.network.MoshiConverter
import com.meloda.app.fast.network.OAuthResultCallFactory
import com.meloda.app.fast.network.ResponseConverterFactory
import com.meloda.app.fast.network.service.account.AccountService
import com.meloda.app.fast.network.service.audios.AudiosService
import com.meloda.app.fast.network.service.auth.AuthService
import com.meloda.app.fast.network.service.conversations.ConversationsService
import com.meloda.app.fast.network.service.files.FilesService
import com.meloda.app.fast.network.service.friends.FriendsService
import com.meloda.app.fast.network.service.longpoll.LongPollService
import com.meloda.app.fast.network.service.messages.MessagesService
import com.meloda.app.fast.network.service.oauth.OAuthService
import com.meloda.app.fast.network.service.photos.PhotosService
import com.meloda.app.fast.network.service.users.UsersService
import com.meloda.app.fast.network.service.videos.VideosService
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.ApiResultConverterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { Moshi.Builder().build() }
    singleOf(::MoshiConverter) bind JsonConverter::class
    single { ChuckerCollector(get()) }
    single { ChuckerInterceptor.Builder(get()).collector(get()).build() }
    singleOf(::AuthInterceptor)
    single {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<ChuckerInterceptor>())
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("${AppConstants.URL_API}/")
            .addConverterFactory(ApiResultConverterFactory)
            .addCallAdapterFactory(ApiResultCallAdapterFactory)
            .addConverterFactory(ResponseConverterFactory(get<JsonConverter>()))
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .client(get())
            .build()
    }

    singleOf(::OAuthResultCallFactory)
    single<Retrofit>(named("oauth")) {
        Retrofit.Builder()
            .baseUrl("${AppConstants.URL_OAUTH}/")
            .addCallAdapterFactory(get<OAuthResultCallFactory>())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .client(get())
            .build()
    }

    single { service(AccountService::class.java) }
    single { service(AudiosService::class.java) }
    single { service(AuthService::class.java) }
    single { service(ConversationsService::class.java) }
    single { service(FilesService::class.java) }
    single { service(LongPollService::class.java) }
    single { service(MessagesService::class.java) }
    single { service(OAuthService::class.java) }
//    single { get<Retrofit>(named("oauth")).create(OAuthService::class.java) }
    single { service(PhotosService::class.java) }
    single { service(UsersService::class.java) }
    single { service(VideosService::class.java) }
    single { service(FriendsService::class.java) }
}

private fun <T> Scope.service(className: Class<T>): T = get<Retrofit>().create(className)
