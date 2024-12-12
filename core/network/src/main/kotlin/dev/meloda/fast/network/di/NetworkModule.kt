package dev.meloda.fast.network.di

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import com.squareup.moshi.Moshi
import dev.meloda.fast.common.AppConstants
import dev.meloda.fast.common.model.LogLevel
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.network.JsonConverter
import dev.meloda.fast.network.MoshiConverter
import dev.meloda.fast.network.OAuthResultCallFactory
import dev.meloda.fast.network.ResponseConverterFactory
import dev.meloda.fast.network.interceptor.LanguageInterceptor
import dev.meloda.fast.network.interceptor.VersionInterceptor
import dev.meloda.fast.network.service.account.AccountService
import dev.meloda.fast.network.service.audios.AudiosService
import dev.meloda.fast.network.service.auth.AuthService
import dev.meloda.fast.network.service.conversations.ConversationsService
import dev.meloda.fast.network.service.files.FilesService
import dev.meloda.fast.network.service.friends.FriendsService
import dev.meloda.fast.network.service.longpoll.LongPollService
import dev.meloda.fast.network.service.messages.MessagesService
import dev.meloda.fast.network.service.oauth.OAuthService
import dev.meloda.fast.network.service.photos.PhotosService
import dev.meloda.fast.network.service.users.UsersService
import dev.meloda.fast.network.service.videos.VideosService
import okhttp3.Interceptor
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
    singleOf(::VersionInterceptor)
    singleOf(::LanguageInterceptor)
    single {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(get(named("token_interceptor")) as Interceptor)
            .addInterceptor(get<VersionInterceptor>())
            .addInterceptor(get<LanguageInterceptor>())
            .addInterceptor(get<ChuckerInterceptor>())
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = when (AppSettings.Debug.networkLogLevel) {
                        LogLevel.NONE -> HttpLoggingInterceptor.Level.NONE
                        LogLevel.BASIC -> HttpLoggingInterceptor.Level.BASIC
                        LogLevel.HEADERS -> HttpLoggingInterceptor.Level.HEADERS
                        LogLevel.BODY -> HttpLoggingInterceptor.Level.BODY
                    }
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
    single { service(PhotosService::class.java) }
    single { service(UsersService::class.java) }
    single { service(VideosService::class.java) }
    single { service(FriendsService::class.java) }
}

private fun <T> Scope.service(className: Class<T>): T = get<Retrofit>().create(className)
