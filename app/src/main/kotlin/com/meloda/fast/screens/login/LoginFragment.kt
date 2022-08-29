package com.meloda.fast.screens.login

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.activity.addCallback
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.base.viewmodel.*
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.DialogCaptchaBinding
import com.meloda.fast.databinding.DialogFastLoginBinding
import com.meloda.fast.databinding.DialogValidationBinding
import com.meloda.fast.databinding.FragmentLoginBinding
import com.meloda.fast.extensions.*
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.screens.settings.SettingsPrefsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.schedule

@AndroidEntryPoint
class LoginFragment : BaseViewModelFragment<LoginViewModel>(R.layout.fragment_login) {

    companion object {
        private const val ArgGetFastToken = "get_fast_token"

        fun newInstance(getFastToken: Boolean = false): LoginFragment {
            val fragment = LoginFragment()
            fragment.arguments = bundleOf(
                ArgGetFastToken to getFastToken
            )

            return fragment
        }
    }

    override val viewModel: LoginViewModel by viewModels()
    private val binding: FragmentLoginBinding by viewBinding()

    private var lastLogin: String = ""
    private var lastPassword: String = ""

    private var errorTimer: Timer? = null

    private var captchaInputLayout: TextInputLayout? = null
    private var validationInputLayout: TextInputLayout? = null

    private var isGetFastToken: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.unknownErrorDefaultText = getString(R.string.unknown_error_occurred)
        isGetFastToken = requireArguments().getBoolean(ArgGetFastToken, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareViews()

        binding.loginInput.clearFocus()

        binding.useCrashReporter.isChecked =
            AppGlobal.preferences.getBoolean(SettingsPrefsFragment.PrefEnableReporter, true)
        binding.useCrashReporter.setOnCheckedChangeListener { _, isChecked ->
            AppGlobal.preferences.edit {
                putBoolean(SettingsPrefsFragment.PrefEnableReporter, isChecked)
                requireActivity().finishAffinity()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            if (getView() == null) {
                isEnabled = false
                return@addCallback
            }

            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                isEnabled = false
            }
        }
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            StartProgressEvent -> onProgressStarted()
            StopProgressEvent -> onProgressStopped()

            is CaptchaRequiredEvent -> showCaptchaDialog(event.sid, event.image)
            is ValidationRequiredEvent -> showValidationRequired(event.sid)

            LoginSuccessAuth -> {
                viewModel.initUserConfig()
                viewModel.openPrimaryScreen()
            }
            LoginCodeSent -> showValidationDialog()
        }
    }

    private fun onProgressStarted() {
        binding.loginContainer.gone()
        binding.passwordContainer.gone()
        binding.auth.gone()
        binding.progressBar.visible()
    }

    private fun onProgressStopped() {
        binding.loginContainer.visible()
        binding.passwordContainer.visible()
        binding.auth.visible()
        binding.progressBar.gone()
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
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    if (getView() == null) return
                    binding.webViewProgressBar.visible()
                    binding.webView.gone()

                    super.onPageStarted(view, url, favicon)
                    parseAuthUrl(url)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    if (getView() == null) return
                    binding.webViewProgressBar.gone()
                    binding.webView.visible()

                    super.onPageFinished(view, url)
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
        binding.webViewContainer.visible()

        val urlToLoad = "https://oauth.vk.com/authorize?client_id=${UserConfig.FAST_APP_ID}&" +
                "access_token=${UserConfig.accessToken}&" +
                "sdk_package=${BuildConfig.sdkPackage}&" +
                "sdk_fingerprint=${BuildConfig.sdkFingerprint}&" +
                "display=page&" +
                "revoke=1&" +
                "scope=${VKConstants.Auth.SCOPE.replace("messages,", "")}&" +
                "redirect_uri=${
                    URLEncoder.encode(
                        "https://oauth.vk.com/blank.html",
                        "utf-8"
                    )
                }&" +
                "response_type=token&" +
                "v=${VKConstants.API_VERSION}"

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

            if (isGetFastToken) {
                val userId = UserConfig.userId
                val accessToken = UserConfig.accessToken

                UserConfig.fastToken = fastToken

                viewModel.saveAccount(userId, accessToken, fastToken)
            } else {
                val account = requireNotNull(viewModel.currentAccount)
                viewModel.currentAccount = account.copy(fastToken = fastToken)
                viewModel.initUserConfig()
            }

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
        binding.auth.setOnLongClickListener {
            showFastLoginAlert()
            true
        }
    }

    private fun showFastLoginAlert() {
        val dialogFastLoginBinding = DialogFastLoginBinding.inflate(layoutInflater, null, false)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.fast_login_title)
            .setView(dialogFastLoginBinding.root)
            .setPositiveButton(R.string.ok) { _, _ ->
                val text = dialogFastLoginBinding.fastLoginText.trimmedText
                if (text.isEmpty()) return@setPositiveButton

                val split = text.split(";")
                try {
                    val login = split[0]
                    val password = split[1]

                    binding.loginInput.setText(login)
                    binding.loginInput.selectLast()

                    binding.passwordInput.setText(password)
                    binding.passwordInput.selectLast()

                    validateDataAndAuth(login to password)
                } catch (ignored: Exception) {
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun validateDataAndAuth(data: Pair<String, String>? = null) {
        if (binding.progressBar.isVisible) return
        val loginString = data?.first ?: binding.loginInput.text.toString().trim()
        val passwordString = data?.second ?: binding.passwordInput.text.toString().trim()

        if (!validateInputData(loginString, passwordString)) return

        lastLogin = loginString
        lastPassword = passwordString

        requireView().findFocus()?.hideKeyboard()

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

        captchaBinding.image.loadWithGlide(
            url = captchaImage,
            crossFade = true
        )
        captchaBinding.image.shapeAppearanceModel =
            captchaBinding.image.shapeAppearanceModel.withCornerSize(16.dpToPx().toFloat())

        val builder = MaterialAlertDialogBuilder(requireContext())
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

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(validationBinding.root)
            .setCancelable(false)
            .setTitle(R.string.input_validation_code)

        val dialog = builder.show()

        validationBinding.ok.setOnClickListener {
            val validationCode = validationBinding.codeInput.trimmedText

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
}