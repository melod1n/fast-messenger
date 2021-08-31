package com.meloda.fast.api.network

import com.meloda.fast.api.VKConstants
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().url.newBuilder()
            .addQueryParameter("v", URLEncoder.encode(VKConstants.API_VERSION, "utf-8"))
        return chain.proceed(chain.request().newBuilder().apply { url(builder.build()) }.build())

    }
}