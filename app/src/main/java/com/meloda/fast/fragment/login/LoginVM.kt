package com.meloda.fast.fragment.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.viewModelScope
import com.meloda.fast.UserConfig
import com.meloda.fast.api.VKAuth
import com.meloda.fast.base.viewmodel.BaseVM
import com.meloda.fast.base.viewmodel.VKEvent
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup

class LoginVM : BaseVM() {

    fun login(
        context: Context,
        email: String,
        password: String,
        captcha: String = ""
    ) {
        val urlToGo = VKAuth.getDirectAuthUrl(email, password, captcha)

//        val builder = AlertDialog.Builder(context)

        val webView = createWebView(context)
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

//        builder.setTitle("Auth")
//        builder.setView(webView)
//        builder.show()

        webView.addJavascriptInterface(WebViewHandlerInterface(), "HtmlHandler")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl(
                    "javascript:window.HtmlHandler.handleHtml" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                )
            }
        }

        webView.loadUrl(urlToGo)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(context: Context): WebView {
        val loginWebView = WebView(context)

        loginWebView.settings.javaScriptEnabled = true
        loginWebView.settings.domStorageEnabled = true
        loginWebView.settings.loadsImagesAutomatically = false
        loginWebView.settings.userAgentString = "Chrome/41.0.2228.0 Safari/537.36"

        loginWebView.clearCache(true)

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        cookieManager.setAcceptCookie(false)

        return loginWebView
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    private fun checkResponse(response: JSONObject) {
        viewModelScope.launch {
            if (response.has("error")) {
                val errorString = response.optString("error")

                when (errorString) {
                    "need_validation" -> {
                        val redirectUrl = response.optString("redirect_uri")

                        tasksEventChannel.send(GoToValidationEvent(redirectUrl))

//                    val bundle = Bundle()
//                    bundle.putString("url", redirectUrl)

                        /* fragment.setFragmentResultListener("validation") { _, bundle ->
                             val userId = bundle.getInt("userId")
                             val token = bundle.getString("token") ?: ""
                             saveUserData(userId, token)

                             openMainScreen()
                         }


                         fragment.parentFragmentManager.beginTransaction()
                             .replace(
                                 R.id.fragmentContainer,
                                 ValidationFragment().apply { arguments = bundle })
                             .addToBackStack("")
                             .commit()*/

                    }
                    "need_captcha" -> {
                        val captchaImage = response.optString("captcha_img")
                        val captchaSid = response.optString("captcha_sid")

                        tasksEventChannel.send(ShowCaptchaDialog(captchaImage, captchaSid))
//                    showCaptchaDialog(captchaImage, captchaSid)
                    }
                }
            } else {
                val userId = response.optInt("user_id", -1)
                val accessToken = response.optString("access_token")

                UserConfig.accessToken = accessToken
                UserConfig.userId = userId

                tasksEventChannel.send(GoToMainEvent)

//            openMainScreen()

//            onResponseListener?.onResponse(null)
            }
        }
    }

    suspend fun getValidatedData(bundle: Bundle) {
        val accessToken = bundle.getString("token") ?: ""
        val userId = bundle.getInt("userId")

        UserConfig.accessToken = accessToken
        UserConfig.userId = userId

        tasksEventChannel.send(GoToMainEvent)
    }

    fun checkUserSession() = viewModelScope.launch {
        if (UserConfig.isLoggedIn()) tasksEventChannel.send(GoToMainEvent)
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
object GoToMainEvent : VKEvent()