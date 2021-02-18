package ru.melod1n.project.vkm.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.*
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.api.UserConfig
import ru.melod1n.project.vkm.api.VKAuth
import ru.melod1n.project.vkm.base.BaseActivity
import ru.melod1n.project.vkm.extensions.ContextExtensions.color
import ru.melod1n.project.vkm.extensions.ContextExtensions.drawable
import ru.melod1n.project.vkm.extensions.DrawableExtensions.tint
import ru.melod1n.project.vkm.widget.Toolbar

class LoginActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var webView: WebView
    private lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()

        prepareToolbar()
        prepareRefreshLayout()

        prepareSettings()

        val url = VKAuth.getUrl(UserConfig.API_ID, VKAuth.settings)

        webView.loadUrl(url)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)
        refreshLayout = findViewById(R.id.refreshLayout)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun prepareSettings() {
        webView.settings.javaScriptEnabled = true
        webView.clearCache(true)
        webView.webViewClient = VKWebClient()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = drawable(R.drawable.ic_close).tint(color(R.color.accent))
        toolbar.setNavigationClickListener { onBackPressed() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun prepareRefreshLayout() {
        refreshLayout.apply {
            setColorSchemeColors(color(R.color.accent))
            setOnRefreshListener {
                webView.reload()
                isRefreshing = false
            }
        }
    }

    private inner class VKWebClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            progressBar.isVisible = true
            view.isVisible = false

            parseUrl(url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)

            progressBar.isVisible = false
            view.isVisible = true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            Log.e("VKM WebView", error.toString())
        }
    }

    private fun parseUrl(url: String) {
        try {
            if (url.startsWith(VKAuth.redirectUrl) && !url.contains("error=")) {
                val auth = VKAuth.parseRedirectUrl(url)
                val token = auth[0]
                val id = auth[1].toInt()

                UserConfig.token = token
                UserConfig.userId = id
                UserConfig.save()

                finishAffinity()
                startActivity(Intent(this, MainActivity::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}