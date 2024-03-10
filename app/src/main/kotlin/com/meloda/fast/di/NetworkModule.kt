package com.meloda.fast.di

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import com.meloda.fast.api.network.AuthInterceptor
import com.meloda.fast.api.network.VkUrls
import com.meloda.fast.base.ResponseConverterFactory
import com.meloda.fast.base.util.MoshiConverter
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.ApiResultConverterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { Moshi.Builder().build() }
    single { MoshiConverter(get()) }
    single { ChuckerCollector(get()) }
    single { ChuckerInterceptor.Builder(get()).collector(get()).build() }
    singleOf(::AuthInterceptor)
    single { GsonBuilder().setLenient().create() }
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
            .baseUrl("${VkUrls.API}/")
            .addConverterFactory(ApiResultConverterFactory)
            .addCallAdapterFactory(ApiResultCallAdapterFactory)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .addConverterFactory(ResponseConverterFactory(get<MoshiConverter>()))
//            .addConverterFactory(GsonConverterFactory.create(get()))
//            .addCallAdapterFactory(ResultCallFactory(get()))
            .client(get())
            .build()
    }
}
