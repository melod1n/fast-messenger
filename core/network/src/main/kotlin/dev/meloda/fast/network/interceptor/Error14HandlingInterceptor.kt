package dev.meloda.fast.network.interceptor

import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.CaptchaTokenResult
import dev.meloda.fast.logger.FastLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class Error14HandlingInterceptor(private val logger: FastLogger) : Interceptor {

    private companion object {
        private const val CAPTCHA_ERROR_CODE = 14
        private const val CAPTCHA_ERROR_KIND = "need_captcha"
        private val executor = Executors.newSingleThreadExecutor()
    }

    private val cookie = AtomicReference<String?>(null)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().withCookie()
        val response = chain.proceed(request)
        response.parseCookie()
        if (request.shouldSkipCaptcha()) return response
        val redirectUri = response.getRedirectUri() ?: return response
        val token = passCaptchaAndGetToken(redirectUri)
        return chain.proceed(chain.request().withCookie().withSuccessToken(token))
    }

    private fun passCaptchaAndGetToken(redirectUri: String): String = synchronized(this) {
        val tokenResult = AtomicReference<Result<String>>(Result.failure(Exception("No result")))

        executor.submit {
            AppSettings.setCaptchaRedirectUri(redirectUri)
            logger.debug(this::class, "passCaptchaAndGetToken: $redirectUri")

            var job: Job? = null
            job = AppSettings.getCaptchaResultFlow()
                .listenValue(CoroutineScope(Dispatchers.IO)) {
                    logger.debug(this::class, "passCaptchaAndGetToken: $it")
                    if (it != CaptchaTokenResult.Initial) {
                        synchronized(tokenResult) {
                            logger.debug(
                                this::class,
                                "passCaptchaAndGetToken: SYNCHRONIZED: $it"
                            )
                            tokenResult.set(wrapResult(it))
                            tokenResult.notifyAll()
                            job?.cancel()
                            logger.debug(
                                this::class,
                                "passCaptchaAndGetToken: NULL RESULT"
                            )
                            AppSettings.setCaptchaResult(CaptchaTokenResult.Initial)
                            AppSettings.setCaptchaRedirectUri(null)
                        }
                    }
                }
        }
        synchronized(tokenResult) {
            if (tokenResult.get().getOrNull() == null) {
                tokenResult.wait()
            }

            logger.debug(this::class, "passCaptchaAndGetToken: GET VALUE")
            tokenResult.get().getOrThrow()
        }
    }

    private fun wrapResult(result: CaptchaTokenResult): Result<String> {
        return when (result) {
            // TODO: 03/05/2026, Danil Nikolaev: check again?
            CaptchaTokenResult.Null -> Result.success("")

            CaptchaTokenResult.Cancelled, CaptchaTokenResult.Initial -> Result.success("")

            is CaptchaTokenResult.Success -> Result.success(result.token)
        }
    }

    private fun Request.withSuccessToken(token: String): Request {
        return newBuilder()
            .url(url.newBuilder().addQueryParameter("success_token", token).build())
            .build()
    }

    private fun Response.getRedirectUri(): String? {
        val responseBody = JSONObject(peekBody(Long.MAX_VALUE).string())
        return if (responseBody.has("error")) {
            val stringError = try {
                responseBody.getString("error")
            } catch (ignored: Exception) {
                null
            }

            if (stringError != null) {
                if (stringError == CAPTCHA_ERROR_KIND && responseBody.has("redirect_uri")) {
                    responseBody.getString("redirect_uri")
                } else {
                    null
                }
            } else {
                val error = responseBody.getJSONObject("error")
                if (error.getInt("error_code") == CAPTCHA_ERROR_CODE) {
                    error.getString("redirect_uri")
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    private fun Request.shouldSkipCaptcha(): Boolean {
        return false
//        return !domains.contains(url.toUrl().host) && domains.isNotEmpty()
    }

    private fun Response.parseCookie() {
        headers("Set-Cookie").firstOrNull { it.contains("remixstlid") }?.let(cookie::set)
    }

    private fun Request.withCookie(): Request {
        return newBuilder().apply { cookie.get()?.let { addHeader("Cookie", it) } }.build()
    }
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "NOTHING_TO_INLINE")
private inline fun Any.wait() = (this as Object).wait()

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "NOTHING_TO_INLINE")
private inline fun Any.notify() = (this as Object).notify()

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "NOTHING_TO_INLINE")
private inline fun Any.notifyAll() = (this as Object).notifyAll()
