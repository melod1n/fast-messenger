package com.meloda.fast.api.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.meloda.fast.api.network.datasource.AuthDataSource
import com.meloda.fast.api.network.repo.AuthRepo
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
class VKModules {

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
    fun provideAuthDataSource(repo: AuthRepo): AuthDataSource =
        AuthDataSource(repo)

}