package ru.melod1n.project.vkm.fragment.ui.repository

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import org.jsoup.Jsoup
import ru.melod1n.project.vkm.api.VKAuth
import ru.melod1n.project.vkm.base.mvp.MvpOnLoadListener
import ru.melod1n.project.vkm.base.mvp.MvpRepository

class LoginRepository : MvpRepository<Any>() {

    fun login(
        context: Context,
        email: String,
        password: String,
        captcha: String,
        onLoadListener: MvpOnLoadListener<JSONObject>
    ) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) return
        val loadingUrl = VKAuth.getDirectAuthUrl(email, password, captcha)

        val webView = createWebView(context)

        webView.addJavascriptInterface(WebViewHandlerInterface(onLoadListener), "HtmlHandler")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl(
                    "javascript:window.HtmlHandler.handleHtml" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                )
            }
        }

        webView.loadUrl(loadingUrl)
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

    private class WebViewHandlerInterface(private var onLoadListener: MvpOnLoadListener<JSONObject>) {
        @JavascriptInterface
        fun handleHtml(html: String?) {
            val doc = Jsoup.parse(html)

            val responseString =
                doc.select("pre[style=\"word-wrap: break-word; white-space: pre-wrap;\"]")
                    .first()
                    .text()

            onLoadListener.onResponse(JSONObject(responseString))
        }
    }
}