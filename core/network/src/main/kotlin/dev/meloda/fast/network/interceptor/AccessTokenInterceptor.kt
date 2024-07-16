package dev.meloda.fast.network.interceptor

import androidx.core.net.toUri
import dev.meloda.fast.common.UserConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class AccessTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().url.newBuilder()

        val uri = builder.build().toUri().toString().toUri()

        if (uri.getQueryParameter("access_token") == null) {
            builder.addQueryParameter(
                "access_token",
                URLEncoder.encode(UserConfig.accessToken, "utf-8")
            )
        }

        return chain.proceed(chain.request().newBuilder().apply { url(builder.build()) }.build())
    }
}
