package dev.meloda.fast.network.interceptor

import androidx.core.net.toUri
import dev.meloda.fast.common.AppConstants
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class VersionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host
        val path = request.url.encodedPath

        // Do not alter upload server URLs (pu.vk.ru / upload.php) which have strict signatures
        if (!host.startsWith("api.vk.") || path.contains("upload.php")) {
            return chain.proceed(request)
        }

        val builder = request.url.newBuilder()
        val uri = builder.build().toUri().toString().toUri()

        if (uri.getQueryParameter("v") == null) {
            builder.addQueryParameter(
                name = "v",
                value = URLEncoder.encode(AppConstants.API_VERSION, "utf-8")
            )
        }

        return chain.proceed(request.newBuilder().apply { url(builder.build()) }.build())
    }
}
