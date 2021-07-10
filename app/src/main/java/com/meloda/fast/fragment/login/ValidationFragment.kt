package com.meloda.fast.fragment.login

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.meloda.fast.R
import com.meloda.fast.api.VKAuth
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.databinding.FragmentValidationBinding

class ValidationFragment : BaseFragment(R.layout.fragment_validation) {

    private val binding: FragmentValidationBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val redirectUrl = getRedirectUrl()

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.d("Fast::Validation", "onPageStarted: url: $url")
                parseUrl(url ?: "")
            }
        }

        binding.webView.settings.domStorageEnabled = true
        binding.webView.clearCache(true)
        binding.webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val manager = CookieManager.getInstance()
        manager.removeAllCookies(null)
        manager.flush()
        manager.setAcceptCookie(true)

        binding.webView.loadUrl(redirectUrl)
    }

    private fun getRedirectUrl() = requireArguments().getString("redirectUrl", "")

    private fun parseUrl(url: String) {
        if (url.startsWith("https://oauth.vk.com/blank.html#success=1")) {
            if (!url.contains("error=")) {
                val data = VKAuth.parseRedirectUrl(url)

                val accessToken = data.first
                val userId = data.second

                parentFragmentManager.setFragmentResult(
                    "validation",
                    bundleOf(
                        "accessToken" to accessToken,
                        "userId" to userId
                    )
                )

                findNavController().navigate(R.id.toLogin)
            }
        } else {
            Log.d("Fast::Validation", "parseUrl: $url")
        }
    }

}