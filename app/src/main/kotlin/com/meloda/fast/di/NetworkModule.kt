package com.meloda.fast.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.network.AuthInterceptor
import com.meloda.fast.api.network.ResultCallFactory
import com.meloda.fast.api.network.VkUrls
import com.meloda.fast.api.network.account.AccountDataSource
import com.meloda.fast.api.network.account.AccountRepo
import com.meloda.fast.api.network.audio.AudiosDataSource
import com.meloda.fast.api.network.audio.AudiosRepo
import com.meloda.fast.api.network.auth.AuthDataSource
import com.meloda.fast.api.network.auth.AuthRepo
import com.meloda.fast.api.network.conversations.ConversationsDataSource
import com.meloda.fast.api.network.conversations.ConversationsRepo
import com.meloda.fast.api.network.files.FilesDataSource
import com.meloda.fast.api.network.files.FilesRepo
import com.meloda.fast.api.network.longpoll.LongPollRepo
import com.meloda.fast.api.network.messages.MessagesDataSource
import com.meloda.fast.api.network.messages.MessagesRepo
import com.meloda.fast.api.network.ota.OtaRepo
import com.meloda.fast.api.network.photos.PhotosDataSource
import com.meloda.fast.api.network.photos.PhotosRepo
import com.meloda.fast.api.network.users.UsersDataSource
import com.meloda.fast.api.network.users.UsersRepo
import com.meloda.fast.api.network.videos.VideosDataSource
import com.meloda.fast.api.network.videos.VideosRepo
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.database.dao.ConversationsDao
import com.meloda.fast.database.dao.MessagesDao
import com.meloda.fast.database.dao.UsersDao
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

    @Singleton
    @Provides
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
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
    fun provideAuthRepo(retrofit: Retrofit): AuthRepo =
        retrofit.create(AuthRepo::class.java)

    @Provides
    @Singleton
    fun provideConversationsRepo(retrofit: Retrofit): ConversationsRepo =
        retrofit.create(ConversationsRepo::class.java)

    @Provides
    @Singleton
    fun provideUsersRepo(retrofit: Retrofit): UsersRepo =
        retrofit.create(UsersRepo::class.java)

    @Provides
    @Singleton
    fun provideMessagesRepo(retrofit: Retrofit): MessagesRepo =
        retrofit.create(MessagesRepo::class.java)

    @Provides
    @Singleton
    fun provideLongPollRepo(retrofit: Retrofit): LongPollRepo =
        retrofit.create(LongPollRepo::class.java)

    @Provides
    @Singleton
    fun provideAuthDataSource(
        repo: AuthRepo
    ): AuthDataSource = AuthDataSource(repo)

    @Provides
    @Singleton
    fun provideUsersDataSource(
        repo: UsersRepo,
        dao: UsersDao
    ): UsersDataSource = UsersDataSource(repo, dao)

    @Provides
    @Singleton
    fun provideConversationsDataSource(
        repo: ConversationsRepo,
        dao: ConversationsDao
    ): ConversationsDataSource = ConversationsDataSource(repo, dao)

    @Provides
    @Singleton
    fun provideMessagesDataSource(
        messagesRepo: MessagesRepo,
        messagesDao: MessagesDao,
        longPollRepo: LongPollRepo
    ): MessagesDataSource = MessagesDataSource(
        messagesRepo = messagesRepo,
        messagesDao = messagesDao,
        longPollRepo = longPollRepo
    )

    @Provides
    @Singleton
    fun provideLongPollUpdatesParser(messagesDataSource: MessagesDataSource): LongPollUpdatesParser =
        LongPollUpdatesParser(messagesDataSource)

    @Provides
    @Singleton
    fun provideAccountRepo(retrofit: Retrofit): AccountRepo =
        retrofit.create(AccountRepo::class.java)

    @Provides
    @Singleton
    fun provideAccountDataSource(repo: AccountRepo): AccountDataSource =
        AccountDataSource(repo)

    @Provides
    @Singleton
    fun provideOtaRepo(retrofit: Retrofit): OtaRepo =
        retrofit.create(OtaRepo::class.java)

    @Provides
    @Singleton
    fun provideUpdateManager(otaRepo: OtaRepo): UpdateManager =
        UpdateManager(otaRepo)

    @Provides
    @Singleton
    fun providePhotosRepo(retrofit: Retrofit): PhotosRepo =
        retrofit.create(PhotosRepo::class.java)

    @Provides
    @Singleton
    fun providePhotosDataSource(photosRepo: PhotosRepo): PhotosDataSource =
        PhotosDataSource(photosRepo)

    @Provides
    @Singleton
    fun provideVideosRepo(retrofit: Retrofit): VideosRepo =
        retrofit.create(VideosRepo::class.java)

    @Provides
    @Singleton
    fun provideVideosDataSource(videosRepo: VideosRepo): VideosDataSource =
        VideosDataSource(videosRepo)

    @Provides
    @Singleton
    fun provideAudiosRepo(retrofit: Retrofit): AudiosRepo =
        retrofit.create(AudiosRepo::class.java)

    @Provides
    @Singleton
    fun provideAudiosDataSource(audiosRepo: AudiosRepo): AudiosDataSource =
        AudiosDataSource(audiosRepo)

    @Provides
    @Singleton
    fun provideFilesRepo(retrofit: Retrofit): FilesRepo =
        retrofit.create(FilesRepo::class.java)

    @Provides
    @Singleton
    fun provideFilesDataSource(filesRepo: FilesRepo): FilesDataSource =
        FilesDataSource(filesRepo)

}