package com.meloda.fast.api.network

import androidx.core.net.toUri
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().url.newBuilder()

        val url = builder.build().toUrl().toString()
        val uri = builder.build().toUri().toString().toUri()

        if (!url.contains("upload.php") && uri.getQueryParameter("v") == null
        ) {
            builder.addQueryParameter("v", URLEncoder.encode(VKConstants.API_VERSION, "utf-8"))
        }

        if (uri.getQueryParameter("access_token") == null && !url.contains("upload.php")) {
            UserConfig.accessToken.let {
                if (it.isNotBlank())
                    builder.addQueryParameter("access_token", URLEncoder.encode(it, "utf-8"))
            }
        }

        return chain.proceed(chain.request().newBuilder().apply { url(builder.build()) }.build())
    }
}
