package com.meloda.fast.di

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.network.AuthInterceptor
import com.meloda.fast.api.network.ResultCallFactory
import com.meloda.fast.api.network.VkUrls
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.data.account.AccountApi
import com.meloda.fast.data.audios.AudiosApi
import com.meloda.fast.data.auth.AuthApi
import com.meloda.fast.data.conversations.ConversationsApi
import com.meloda.fast.data.files.FilesApi
import com.meloda.fast.data.longpoll.LongPollApi
import com.meloda.fast.data.messages.MessagesApi
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.data.ota.OtaApi
import com.meloda.fast.data.photos.PhotosApi
import com.meloda.fast.data.users.UsersApi
import com.meloda.fast.data.videos.VideosApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    /*

    val chuckerCollector = ChuckerCollector(
        context = this,
        // Toggles visibility of the notification
        showNotification = true,
        // Allows to customize the retention period of collected data
        retentionPeriod = RetentionManager.Period.ONE_HOUR
)

// Create the Interceptor
val chuckerInterceptor = ChuckerInterceptor.Builder(context)
        // The previously created Collector
        .collector(chuckerCollector)
        // The max body content length in bytes, after this responses will be truncated.
        .maxContentLength(250_000L)
        // List of headers to replace with ** in the Chucker UI
        .redactHeaders("Auth-Token", "Bearer")
        // Read the whole response body even when the client does not consume the response completely.
        // This is useful in case of parsing errors or when the response body
        // is closed before being read like in Retrofit with Void and Unit types.
        .alwaysReadResponseBody(true)
        // Use decoder when processing request and response bodies. When multiple decoders are installed they
        // are applied in an order they were added.
        .addBodyDecoder(decoder)
        // Controls Android shortcut creation. Available in SNAPSHOTS versions only at the moment
        .createShortcut(true)
        .build()
     */

    @Singleton
    @Provides
    fun provideChuckerCollector(): ChuckerCollector =
        ChuckerCollector(AppGlobal.Instance)

    @Singleton
    @Provides
    fun provideChuckerInterceptor(
        chuckerCollector: ChuckerCollector
    ): ChuckerInterceptor =
        ChuckerInterceptor.Builder(AppGlobal.Instance)
            .collector(chuckerCollector)
            .build()

    @Singleton
    @Provides
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(chuckerInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            ).build()

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("${VkUrls.API}/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(ResultCallFactory())
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor = AuthInterceptor()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideConversationsApi(retrofit: Retrofit): ConversationsApi =
        retrofit.create(ConversationsApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi =
        retrofit.create(UsersApi::class.java)

    @Provides
    @Singleton
    fun provideMessagesApi(retrofit: Retrofit): MessagesApi =
        retrofit.create(MessagesApi::class.java)

    @Provides
    @Singleton
    fun provideLongPollApi(retrofit: Retrofit): LongPollApi =
        retrofit.create(LongPollApi::class.java)

    @Provides
    @Singleton
    fun provideLongPollUpdatesParser(messagesRepository: MessagesRepository): LongPollUpdatesParser =
        LongPollUpdatesParser(messagesRepository)

    @Provides
    @Singleton
    fun provideAccountApi(retrofit: Retrofit): AccountApi =
        retrofit.create(AccountApi::class.java)

    @Provides
    @Singleton
    fun provideOtaApi(retrofit: Retrofit): OtaApi =
        retrofit.create(OtaApi::class.java)

    @Provides
    @Singleton
    fun provideUpdateManager(otaApi: OtaApi): UpdateManager =
        UpdateManager(otaApi)

    @Provides
    @Singleton
    fun providePhotosApi(retrofit: Retrofit): PhotosApi =
        retrofit.create(PhotosApi::class.java)

    @Provides
    @Singleton
    fun provideVideosApi(retrofit: Retrofit): VideosApi =
        retrofit.create(VideosApi::class.java)

    @Provides
    @Singleton
    fun provideAudiosApi(retrofit: Retrofit): AudiosApi =
        retrofit.create(AudiosApi::class.java)

    @Provides
    @Singleton
    fun provideFilesApi(retrofit: Retrofit): FilesApi =
        retrofit.create(FilesApi::class.java)

}