package com.meloda.app.fast.network

import androidx.core.net.toUri
import com.meloda.app.fast.common.AppConstants
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class AuthInterceptor(private val accessToken: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().url.newBuilder()

        val uri = builder.build().toUri().toString().toUri()

        if (uri.getQueryParameter("v") == null) {
            builder.addQueryParameter(
                name = "v",
                value = URLEncoder.encode(AppConstants.API_VERSION, "utf-8")
            )
        }

        if (accessToken.isNotBlank()) {
            builder.addQueryParameter("access_token", URLEncoder.encode(accessToken, "utf-8"))
        }

        return chain.proceed(chain.request().newBuilder().apply { url(builder.build()) }.build())
    }
}
