package com.meloda.fast.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.meloda.fast.api.network.AuthInterceptor
import com.meloda.fast.api.network.ResultCallFactory
import com.meloda.fast.api.network.datasource.AuthDataSource
import com.meloda.fast.api.network.datasource.ConversationsDataSource
import com.meloda.fast.api.network.datasource.MessagesDataSource
import com.meloda.fast.api.network.datasource.UsersDataSource
import com.meloda.fast.api.network.repo.*
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
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

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
        .baseUrl("https://api.vk.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(ResultCallFactory())
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor = AuthInterceptor()

    @Provides
    fun provideAuthRepo(retrofit: Retrofit): AuthRepo =
        retrofit.create(AuthRepo::class.java)

    @Provides
    fun provideConversationsRepo(retrofit: Retrofit): ConversationsRepo =
        retrofit.create(ConversationsRepo::class.java)

    @Provides
    fun provideUsersRepo(retrofit: Retrofit): UsersRepo =
        retrofit.create(UsersRepo::class.java)

    @Provides
    fun provideMessagesRepo(retrofit: Retrofit): MessagesRepo =
        retrofit.create(MessagesRepo::class.java)

    @Provides
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
        repo: MessagesRepo,
        dao: MessagesDao
    ): MessagesDataSource = MessagesDataSource(repo, dao)
}