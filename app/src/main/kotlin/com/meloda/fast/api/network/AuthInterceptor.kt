package com.meloda.fast.api.network

import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.network.account.AccountUrls
import com.meloda.fast.api.network.ota.OtaUrls
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().url.newBuilder()

        val url = builder.build().toUrl().toString()

        if (!url.contains("upload.php") && !url.contains(OtaUrls.GetActualUrl)) {
            builder.addQueryParameter("v", URLEncoder.encode(VKConstants.API_VERSION, "utf-8"))
        }

        if (!url.contains(AccountUrls.SetOnline) && !url.contains("upload.php")) {
            UserConfig.accessToken.let {
                if (it.isNotBlank())
                    builder.addQueryParameter("access_token", URLEncoder.encode(it, "utf-8"))
            }
        }

        return chain.proceed(chain.request().newBuilder().apply { url(builder.build()) }.build())

    }
}