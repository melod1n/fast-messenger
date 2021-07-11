package com.meloda.fast.fragment.login

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.viewModelScope
import com.meloda.fast.UserConfig
import com.meloda.fast.api.VKAuth
import com.meloda.fast.base.viewmodel.BaseVM
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup

class LoginVM : BaseVM() {

    private var isWebViewPrepared = true

    suspend fun login(
        webView: WebView,
        email: String,
        password: String,
        captcha: String = ""
    ) {
        sendEvent(StartProgressEvent)

        val urlToGo = VKAuth.getDirectAuthUrl(email, password, captcha)

        if (isWebViewPrepared) {
            isWebViewPrepared = false

            webView.addJavascriptInterface(WebViewHandlerInterface(), "HtmlHandler")

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    webView.loadUrl(
                        "javascript:window.HtmlHandler.handleHtml" +
                                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                    )
                }
            }
        }

        webView.loadUrl(urlToGo)
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    private fun checkResponse(response: JSONObject) {
        viewModelScope.launch(Dispatchers.Default) {
            delay(1500)
            sendEvent(StopProgressEvent)

            if (response.has("error")) {
                val errorString = response.optString("error")

                when (errorString) {
                    "need_validation" -> {
                        val redirectUrl = response.optString("redirect_uri")

                        tasksEventChannel.send(GoToValidationEvent(redirectUrl))
                    }
                    "need_captcha" -> {
                        val captchaImage = response.optString("captcha_img")
                        val captchaSid = response.optString("captcha_sid")

                        tasksEventChannel.send(ShowCaptchaDialog(captchaImage, captchaSid))
                    }
                }
            } else {
                val userId = response.optInt("user_id", -1)
                val accessToken = response.optString("access_token")

                UserConfig.accessToken = accessToken
                UserConfig.userId = userId

                tasksEventChannel.send(GoToMainEvent())
            }
        }
    }

    suspend fun getValidatedData(bundle: Bundle) {
        val accessToken = bundle.getString("token") ?: ""
        val userId = bundle.getInt("userId")

        UserConfig.accessToken = accessToken
        UserConfig.userId = userId

        tasksEventChannel.send(GoToMainEvent())
    }

    inner class WebViewHandlerInterface {
        @JavascriptInterface
        fun handleHtml(html: String) {
            val doc = Jsoup.parse(html)

            val responseString =
                doc.select("pre[style=\"word-wrap: break-word; white-space: pre-wrap;\"]").first()
                    ?.text() ?: ""

            checkResponse(JSONObject(responseString))
        }
    }

}

data class ShowCaptchaDialog(val captchaImage: String, val captchaSid: String) : VKEvent()
data class GoToValidationEvent(val redirectUrl: String) : VKEvent()
data class GoToMainEvent(val haveAuthorized: Boolean = true) : VKEvent()