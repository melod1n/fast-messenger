package dev.meloda.fast.network.interceptor

import androidx.core.net.toUri
import dev.meloda.fast.common.AppConstants
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class VersionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().url.newBuilder()

        val uri = builder.build().toUri().toString().toUri()

        if (uri.getQueryParameter("v") == null) {
            builder.addQueryParameter(
                name = "v",
                value = URLEncoder.encode(AppConstants.API_VERSION, "utf-8")
            )
        }

        return chain.proceed(chain.request().newBuilder().apply { url(builder.build()) }.build())
    }
}
