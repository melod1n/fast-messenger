package com.meloda.fast.screens.login

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.CaptchaRequiredEvent
import com.meloda.fast.base.viewmodel.ValidationRequiredEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.DialogCaptchaBinding
import com.meloda.fast.databinding.DialogFastLoginBinding
import com.meloda.fast.databinding.DialogValidationBinding
import com.meloda.fast.databinding.FragmentLoginBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.color
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.flowOnLifecycle
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.hideKeyboard
import com.meloda.fast.ext.notifyAboutChanges
import com.meloda.fast.ext.selectLast
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.trimmedText
import com.meloda.fast.ext.visible
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.screens.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import java.net.URLEncoder
import java.util.regex.Pattern

@AndroidEntryPoint
class LoginFragment : BaseViewModelFragment<LoginViewModel>(R.layout.fragment_login) {

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override val viewModel: LoginViewModel by viewModels()
    private val binding by viewBinding(FragmentLoginBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backgroundColor = color(R.color.colorLoginFragmentBackground)

        requireActivity().window.apply {
            statusBarColor = backgroundColor
            navigationBarColor = backgroundColor
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareView()

        val roundedCorners = 10.dpToPx().toFloat()
        val onFocusedChangedListener = View.OnFocusChangeListener { editText, hasFocus ->
            val inputLayout = editText.parent.parent as? TextInputLayout
                ?: throw NullPointerException("Something in layout was changed")
            val cornerRadiusToSet = if (hasFocus) 0F else roundedCorners

            if (inputLayout.boxCornerRadiusBottomEnd != cornerRadiusToSet) {
                ValueAnimator.ofFloat(
                    inputLayout.boxCornerRadiusBottomEnd,
                    cornerRadiusToSet
                ).apply {
                    duration = 100
                    interpolator = LinearInterpolator()

                    addUpdateListener { animator ->
                        val value = animator.animatedValue as Float
                        inputLayout.setBoxCornerRadii(roundedCorners, roundedCorners, value, value)
                    }
                }.start()
            }
        }

        binding.login.onFocusChangeListener = onFocusedChangedListener
        binding.password.onFocusChangeListener = onFocusedChangedListener

        binding.login.notifyAboutChanges(viewModel.loginValue)
        binding.password.notifyAboutChanges(viewModel.passwordValue)

        viewModel.isLoginValid.flowOnLifecycle(lifecycle) { isValid ->
            binding.loginContainer.error =
                if (isValid) null
                else getString(R.string.input_login_hint)
        }
        viewModel.isPasswordValid.flowOnLifecycle(lifecycle) { isValid ->
            binding.passwordContainer.error =
                if (isValid) null
                else getString(R.string.input_password_hint)
        }

        binding.useCrashReporter.isChecked =
            AppGlobal.preferences.getBoolean(SettingsFragment.KEY_MS_APPCENTER_ENABLE, true)
        binding.useCrashReporter.setOnCheckedChangeListener { _, isChecked ->
            AppGlobal.preferences.edit {
                putBoolean(SettingsFragment.KEY_MS_APPCENTER_ENABLE, isChecked)
                requireActivity().finishAffinity()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
        }
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            is CaptchaRequiredEvent -> showCaptchaDialog(event.sid, event.image)
            is ValidationRequiredEvent -> showValidationRequired(event.sid)

            LoginSuccessAuth -> {
                viewModel.initUserConfig()
                viewModel.openPrimaryScreen()
            }
            LoginCodeSent -> showValidationDialog()
        }
    }

    override fun toggleProgress(isProgressing: Boolean) {
        binding.progressBar.toggleVisibility(isProgressing)

        val newScale = if (binding.signIn.isVisible) 0F else 1F

        ValueAnimator.ofFloat(binding.signIn.scaleX, newScale).apply {
            duration = 100
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                if (view == null) return@addUpdateListener

                val value = animator.animatedValue as Float

                binding.signIn.scaleX = value
                binding.signIn.scaleY = value

                binding.signIn.toggleVisibility(value > 0)
            }
        }.start()
    }

    private fun prepareView() {
        applyInsets()
        prepareFields()
        prepareWebView()
        prepareAuthButton()
    }

    private fun applyInsets() {
        binding.root.applyInsetter {
            type(ime = true, statusBars = true) { padding() }
        }
    }

    private fun prepareFields() {
        binding.login.clearFocus()

        binding.password.typeface = Typeface.DEFAULT
        binding.passwordContainer.endIconMode = TextInputLayout.END_ICON_NONE

        binding.password.setOnEditorActionListener edit@{ _, _, event ->
            if (event == null) return@edit false
            return@edit if (event.action == EditorInfo.IME_ACTION_GO ||
                (event.action == KeyEvent.ACTION_DOWN && (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER))
            ) {
                binding.password.hideKeyboard()
                binding.signIn.performClick()
                true
            } else false
        }
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

            val account = requireNotNull(viewModel.currentAccount)
            viewModel.currentAccount = account.copy(fastToken = fastToken)
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

    private fun prepareAuthButton() {
        binding.signIn.setOnClickListener {
            viewModel.login()
        }
        binding.signIn.setOnLongClickListener {
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

                    binding.login.setText(login)
                    binding.login.selectLast()

                    binding.password.setText(password)
                    binding.password.selectLast()

                    viewModel.login()
                } catch (ignored: Exception) {
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCaptchaDialog(captchaSid: String, captchaImage: String) {
        val captchaBinding = DialogCaptchaBinding.inflate(layoutInflater, null, false)

        captchaBinding.image.loadWithGlide {
            imageUrl = captchaImage
            crossFade = true
        }
        captchaBinding.image.shapeAppearanceModel =
            captchaBinding.image.shapeAppearanceModel.withCornerSize(16.dpToPx().toFloat())

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(captchaBinding.root)
            .setCancelable(false)
            .setTitle(R.string.input_captcha)

        val dialog = builder.show()

        captchaBinding.ok.setOnClickListener {
            val captchaCode = captchaBinding.captchaInput.trimmedText
            viewModel.captchaValue.value = "$captchaSid;$captchaCode"

            dialog.dismiss()
            viewModel.login()
        }
        captchaBinding.cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showValidationDialog() {
        val validationBinding = DialogValidationBinding.inflate(layoutInflater, null, false)

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(validationBinding.root)
            .setCancelable(false)
            .setTitle(R.string.input_validation_code)

        val dialog = builder.show()

        validationBinding.ok.setOnClickListener {
            val validationCode = validationBinding.codeInput.trimmedText
            viewModel.twoFaValue.value = validationCode

            dialog.dismiss()
            viewModel.login()
        }
        validationBinding.cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showValidationRequired(validationSid: String) {
        Toast.makeText(requireContext(), R.string.validation_required, Toast.LENGTH_LONG).show()
        viewModel.sendSms(validationSid)
    }

    override fun onDestroy() {
        super.onDestroy()

        requireActivity().window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }
}
