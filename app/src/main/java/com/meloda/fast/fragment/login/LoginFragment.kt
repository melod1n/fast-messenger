package com.meloda.fast.fragment.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.viewbinding.library.fragment.viewBinding
import android.webkit.CookieManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.R
import com.meloda.fast.base.BaseVMFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import com.meloda.fast.databinding.FragmentLoginBinding
import com.meloda.fast.fragment.main.MainFragment
import com.meloda.fast.util.KeyboardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToInt

@AndroidEntryPoint
class LoginFragment : BaseVMFragment<LoginVM>(R.layout.fragment_login) {

    override val viewModel: LoginVM by viewModels()
    private val binding: FragmentLoginBinding by viewBinding()

    private var lastEmail: String = ""
    private var lastPassword: String = ""

    private var errorTimer: Timer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (parentFragment?.parentFragment as? MainFragment)?.bottomBar?.isVisible = false

        prepareViews()

        setFragmentResultListener("validation") { _, bundle ->
            lifecycleScope.launch { viewModel.getValidatedData(bundle) }
        }
    }

    override fun onEvent(event: VKEvent) {
        super.onEvent(event)

        when (event) {
            is ShowCaptchaDialog -> showCaptchaDialog(event.captchaImage, event.captchaSid)
            is GoToValidationEvent -> goToValidation(event.redirectUrl)
            is GoToMainEvent -> goToMain(event.haveAuthorized)
            StartProgressEvent -> onProgressStarted()
            StopProgressEvent -> onProgressEnded()
        }
    }

    private fun onProgressStarted() {
        binding.loginContainer.isVisible = false
        binding.passwordContainer.isVisible = false
        binding.auth.isVisible = false
        binding.progress.isVisible = true
    }

    private fun onProgressEnded() {
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
            settings.loadsImagesAutomatically = false
            settings.userAgentString = "Chrome/41.0.2228.0 Safari/537.36"
            clearCache(true)
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        cookieManager.setAcceptCookie(false)
    }

    private fun prepareEmailEditText() {
        binding.loginInput.addTextChangedListener {
            if (!binding.loginLayout.error.isNullOrBlank()) binding.loginLayout.error = ""
        }
    }

    private fun preparePasswordEditText() {
        binding.passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE

        binding.passwordInput.addTextChangedListener {
            if (!binding.passwordLayout.error.isNullOrBlank()) binding.passwordLayout.error = ""
        }

        binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
            binding.passwordLayout.endIconMode =
                if (hasFocus) TextInputLayout.END_ICON_PASSWORD_TOGGLE
                else TextInputLayout.END_ICON_NONE
        }

        binding.passwordInput.setOnEditorActionListener { _, _, event ->
            if (event == null) return@setOnEditorActionListener false
            return@setOnEditorActionListener if (event.action == EditorInfo.IME_ACTION_GO ||
                (event.action == KeyEvent.ACTION_DOWN && (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER))
            ) {
                KeyboardUtils.hideKeyboardFrom(binding.passwordInput)
                binding.auth.performClick()
                true
            } else false
        }
    }

    private fun prepareAuthButton() {
        binding.auth.setOnClickListener {
            if (binding.progress.isVisible) return@setOnClickListener

            val loginString = binding.loginInput.text.toString().trim()
            val passwordString = binding.passwordInput.text.toString().trim()

            if (!validateInputData(loginString, passwordString)) return@setOnClickListener

            KeyboardUtils.hideKeyboardFrom(it)

            lifecycleScope.launch {
                viewModel.login(
                    binding.webView,
                    loginString,
                    passwordString
                )
            }
        }
    }

    private fun validateInputData(loginString: String, passwordString: String): Boolean {
        var isValidated = true

        if (loginString.isEmpty()) {
            isValidated = false
            setError("Input login", binding.loginLayout)
        }

        if (passwordString.isEmpty()) {
            isValidated = false
            setError("Input password", binding.passwordLayout)
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
    }

    // TODO: 7/10/2021 extract layout to resources
    private fun showCaptchaDialog(captchaImage: String, captchaSid: String) {
        val metrics = resources.displayMetrics

        val width = (metrics.widthPixels / 3.5).roundToInt()
        val height = metrics.heightPixels / 7

        val image = ShapeableImageView(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(width, height)
        }

        val shapeModel = image.shapeAppearanceModel
        image.shapeAppearanceModel = shapeModel.withCornerSize { 12f }

        image.load(captchaImage) { crossfade(100) }

        val captchaCodeEditText = TextInputEditText(requireContext())
        captchaCodeEditText.setHint(R.string.captcha_hint)

        captchaCodeEditText.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        val builder = AlertDialog.Builder(requireContext())

        val layout = LinearLayout(requireContext())

        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.addView(image)
        layout.addView(captchaCodeEditText)

        builder.setView(layout)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val captchaCode = captchaCodeEditText.text.toString().trim()

            lifecycleScope.launch {
                viewModel.login(
                    binding.webView,
                    lastEmail,
                    lastPassword,
                    "&captcha_sid=$captchaSid&captcha_key=$captchaCode"
                )
            }
        }

        builder.setTitle(R.string.input_captcha)
        builder.show()
    }

    private fun goToValidation(redirectUrl: String) {
        findNavController().navigate(
            R.id.toValidation,
            bundleOf("redirectUrl" to redirectUrl)
        )
    }

    private fun goToMain(haveAuthorized: Boolean) {
        lifecycleScope.launch {
            if (haveAuthorized) delay(500)

            findNavController().navigate(R.id.toMain)
        }
    }

}