package com.meloda.fast.di

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import com.meloda.fast.api.network.AuthInterceptor
import com.meloda.fast.api.network.ResultCallFactory
import com.meloda.fast.api.network.VkUrls
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { Moshi.Builder().build() }
    single { ChuckerCollector(get()) }
    single { ChuckerInterceptor.Builder(get()).collector(get()).build() }
    singleOf(::AuthInterceptor)
    single { GsonBuilder().setLenient().create() }
    single {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor())
            .addInterceptor(
                chuckerInterceptor().apply {
                    redactHeader("Secret-Code")
                }
            )
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            ).build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("${VkUrls.API}/")
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(ResultCallFactory(get()))
            .client(get())
            .build()
    }
}

internal fun Scope.retrofit(): Retrofit = get()
private fun Scope.authInterceptor(): AuthInterceptor = get()
private fun Scope.chuckerInterceptor(): ChuckerInterceptor = get()
