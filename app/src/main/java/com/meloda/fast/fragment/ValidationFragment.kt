package com.meloda.fast.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import com.meloda.fast.base.BaseFragment
import com.meloda.vksdk.VKAuth


class ValidationFragment : BaseFragment() {

    private var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null && requireArguments().isEmpty.not()) {
            url = requireArguments().getString("url") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val webView = WebView(requireContext())
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                parseUrl(url ?: "")
            }
        }

        webView.settings.domStorageEnabled = true
        webView.clearCache(true)
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val manager = CookieManager.getInstance()
        manager.removeAllCookies(null)
        manager.flush()
        manager.setAcceptCookie(true)

        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireView() as WebView).loadUrl(url)
    }

    private fun parseUrl(url: String) {
        Log.d("WebView url", url)
        try {
            if (url.startsWith("https://oauth.vk.com/blank.html#success=1")) {
                Log.d("Success WebView", "")
                if (!url.contains("error=")) {
                    val auth = VKAuth.parseRedirectUrl(url)

                    val token = auth[0]
                    val userId = auth[1].toInt()

                    parentFragmentManager.setFragmentResult(
                        "validation",
                        bundleOf(
                            Pair("token", token),
                            Pair("userId", userId)
                        )
                    )

                    parentFragmentManager.popBackStack()

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}