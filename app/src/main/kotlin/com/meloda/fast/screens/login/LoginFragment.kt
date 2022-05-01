package com.meloda.fast.screens.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.viewbinding.library.fragment.viewBinding
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.*
import com.meloda.fast.databinding.DialogCaptchaBinding
import com.meloda.fast.databinding.DialogValidationBinding
import com.meloda.fast.databinding.FragmentLoginBinding
import com.meloda.fast.extensions.hideKeyboard
import com.meloda.fast.extensions.invisible
import com.meloda.fast.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.schedule

@AndroidEntryPoint
class LoginFragment : BaseViewModelFragment<LoginViewModel>(R.layout.fragment_login) {

    override val viewModel: LoginViewModel by viewModels()
    private val binding: FragmentLoginBinding by viewBinding()

    private var lastLogin: String = ""
    private var lastPassword: String = ""

    private var errorTimer: Timer? = null

    private var captchaInputLayout: TextInputLayout? = null
    private var validationInputLayout: TextInputLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.unknownErrorDefaultText = getString(R.string.unknown_error_occurred)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareViews()

        binding.loginInput.clearFocus()
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            StartProgressEvent -> onProgressStarted()
            StopProgressEvent -> onProgressStopped()
            is ErrorEvent -> showErrorSnackbar(event.errorText)
            is CaptchaEvent -> showCaptchaDialog(event.sid, event.image)
            is ValidationEvent -> showValidationRequired(event.sid)

            LoginViewModel.LoginSuccessAuth -> launchWebView()
            LoginViewModel.LoginCodeSent -> showValidationDialog()
        }
    }

    private fun onProgressStarted() {
        binding.loginContainer.isVisible = false
        binding.passwordContainer.isVisible = false
        binding.auth.isVisible = false
        binding.progress.isVisible = true
    }

    private fun onProgressStopped() {
        binding.loginContainer.isVisible = true
        binding.passwordContainer.isVisible = true
        binding.auth.isVisible = true
        binding.progress.isVisible = false
    }

    private fun prepareViews() {
        prepareWebView()
        prepareEmailEditText()
        preparePasswordEditText()
        prepareAuthButton()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun prepareWebView() {
        with(binding.webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            clearCache(true)
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    parseAuthUrl(url)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)

                    lifecycleScope.launch {
//                        delay(500)
                        withContext(Dispatchers.Main) {
                            view.loadUrl("javascript:document.getElementsByClassName(\"button\")[0].click()")
                        }
                    }

                    if (url.contains("oauth.vk.com/authorize?")) {

                    }
                }
            }
        }

        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
            setAcceptCookie(true)
        }
    }

    private fun launchWebView() {
        val urlToLoad = "https://oauth.vk.com/authorize?client_id=${UserConfig.FAST_APP_ID}&" +
                "access_token=${UserConfig.accessToken}&" +
                "sdk_package=com.meloda.fast.activity&" +
                "sdk_fingerprint=AA88DSADAS8DG8FSA8&" +
                "display=page&" +
                "revoke=1&" +
                "scope=136297695&" +
                "redirect_uri=${
                    URLEncoder.encode(
                        "https://oauth.vk.com/blank.html",
                        Charsets.UTF_8.toString()
                    )
                }&" +
                "response_type=token&" +
                "v=${VKConstants.API_VERSION}"

        binding.progress.visible()
        binding.webView.invisible()
        binding.webView.loadUrl(urlToLoad)
    }

    private fun parseAuthUrl(url: String) {
        if (url.isBlank()) return

        if (url.startsWith("https://oauth.vk.com/blank.html")) {
            if (url.contains("error")) {
                Log.e("Fast::Login", "errorUrl: $url")
                return
            }

            val authData = parseRedirectUrl(url)
            if (authData == null) {
                Log.e("Fast::Login", "errorUrl: $url")
                return
            }

            val fastToken = authData.first

            viewModel.currentAccount = viewModel.currentAccount?.copy(fastToken = fastToken)
            viewModel.initUserConfig()

            viewModel.openPrimaryScreen()
        }
    }

    private fun parseRedirectUrl(url: String): Pair<String, Int>? {
        val accessToken = extractPattern(url, "access_token=(.*?)&") ?: return null
        val userId = extractPattern(url, "id=(\\d*)")?.toIntOrNull() ?: return null

        return accessToken to userId
    }

    private fun extractPattern(string: String, pattern: String): String? {
        val p = Pattern.compile(pattern)
        val m = p.matcher(string)
        return if (m.find()) {
            m.group(1)
        } else null
    }

    private fun prepareEmailEditText() {
        binding.loginInput.addTextChangedListener {
            if (!binding.loginLayout.error.isNullOrBlank()) binding.loginLayout.error = ""
        }
    }

    private fun preparePasswordEditText() {
        binding.passwordInput.typeface = Typeface.DEFAULT
        binding.passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE

        binding.passwordInput.addTextChangedListener {
            if (!binding.passwordLayout.error.isNullOrBlank()) binding.passwordLayout.error = ""
        }

        binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
            binding.passwordLayout.endIconMode =
                if (hasFocus) TextInputLayout.END_ICON_PASSWORD_TOGGLE
                else TextInputLayout.END_ICON_NONE
        }

        binding.passwordInput.setOnEditorActionListener edit@{ _, _, event ->
            if (event == null) return@edit false
            return@edit if (event.action == EditorInfo.IME_ACTION_GO ||
                (event.action == KeyEvent.ACTION_DOWN && (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER))
            ) {
                binding.passwordInput.hideKeyboard()
                binding.auth.performClick()
                true
            } else false
        }
    }

    private fun prepareAuthButton() {
        binding.auth.setOnClickListener { validateDataAndAuth() }
    }

    private fun validateDataAndAuth(data: Pair<String, String>? = null) {
        if (binding.progress.isVisible) return
        val loginString = data?.first ?: binding.loginInput.text.toString().trim()
        val passwordString = data?.second ?: binding.passwordInput.text.toString().trim()

        if (!validateInputData(loginString, passwordString)) return

        lastLogin = loginString
        lastPassword = passwordString

        requireView().findFocus().hideKeyboard()

        viewModel.login(
            login = loginString,
            password = passwordString
        )
    }

    private fun validateInputData(
        loginString: String?,
        passwordString: String?,
        captchaCode: String? = null,
        validationCode: String? = null
    ): Boolean {
        var isValidated = true

        if (loginString?.isEmpty() == true) {
            isValidated = false
            setError(getString(R.string.input_login_hint), binding.loginLayout)
        }

        if (passwordString?.isEmpty() == true) {
            isValidated = false
            setError(getString(R.string.input_password_hint), binding.passwordLayout)
        }

        if (captchaCode?.isEmpty() == true && captchaInputLayout != null) {
            isValidated = false
            setError(getString(R.string.input_code_hint), captchaInputLayout!!)
        }

        if (validationCode?.isEmpty() == true && validationInputLayout != null) {
            isValidated = false
            setError(getString(R.string.input_code_hint), validationInputLayout!!)
        }

        return isValidated
    }

    private fun setError(error: String, inputLayout: TextInputLayout) {
        inputLayout.error = error

        if (errorTimer != null) {
            errorTimer?.cancel()
            errorTimer = null
        }

        if (errorTimer == null) {
            errorTimer = Timer()
        }

        errorTimer?.schedule(2500) {
            lifecycleScope.launch(Dispatchers.Main) { clearErrors() }
        }
    }

    private fun clearErrors() {
        binding.loginLayout.error = ""
        binding.passwordLayout.error = ""

        captchaInputLayout?.error = ""
    }

    private fun showCaptchaDialog(captchaSid: String, captchaImage: String) {
        val captchaBinding = DialogCaptchaBinding.inflate(layoutInflater, null, false)
        captchaInputLayout = captchaBinding.captchaLayout

        captchaBinding.image.load(captchaImage) {
            crossfade(100)
            transformations(RoundedCornersTransformation(4f))
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(captchaBinding.root)
            .setCancelable(false)
            .setTitle(R.string.input_captcha)

        val dialog = builder.show()

        captchaBinding.ok.setOnClickListener {
            val captchaCode = captchaBinding.captchaInput.text.toString().trim()

            if (!validateInputData(
                    loginString = null,
                    passwordString = null,
                    captchaCode = captchaCode
                )
            ) return@setOnClickListener

            dialog.dismiss()

            viewModel.login(
                login = lastLogin,
                password = lastPassword,
                captcha = captchaSid to captchaCode
            )
        }
        captchaBinding.cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showValidationDialog() {
        val validationBinding = DialogValidationBinding.inflate(layoutInflater, null, false)
        validationInputLayout = validationBinding.codeLayout

        val builder = AlertDialog.Builder(requireContext())
            .setView(validationBinding.root)
            .setCancelable(false)
            .setTitle(R.string.input_validation_code)

        val dialog = builder.show()

        validationBinding.ok.setOnClickListener {
            val validationCode = validationBinding.codeInput.text.toString().trim()

            if (!validateInputData(
                    loginString = null,
                    passwordString = null,
                    validationCode = validationCode
                )
            ) return@setOnClickListener

            dialog.dismiss()

            viewModel.login(
                login = lastLogin,
                password = lastPassword,
                twoFaCode = validationCode
            )
        }
        validationBinding.cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showValidationRequired(validationSid: String) {
        Toast.makeText(requireContext(), R.string.validation_required, Toast.LENGTH_LONG).show()
        viewModel.sendSms(validationSid)
    }

    private fun showErrorSnackbar(errorDescription: String) {
        val snackbar = Snackbar.make(
            requireView(),
            getString(R.string.error, errorDescription),
            Snackbar.LENGTH_LONG
        )

        snackbar.animationMode = Snackbar.ANIMATION_MODE_FADE
        snackbar.show()
    }
}