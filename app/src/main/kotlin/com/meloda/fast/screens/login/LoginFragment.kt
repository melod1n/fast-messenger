package com.meloda.fast.screens.login

import android.graphics.Typeface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.viewbinding.library.fragment.viewBinding
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.base.BaseVMFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import com.meloda.fast.databinding.DialogCaptchaBinding
import com.meloda.fast.databinding.FragmentLoginBinding
import com.meloda.fast.screens.main.MainFragment
import com.meloda.fast.util.KeyboardUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

@AndroidEntryPoint
class LoginFragment : BaseVMFragment<LoginViewModel>(R.layout.fragment_login) {

    override val viewModel: LoginViewModel by viewModels()
    private val binding: FragmentLoginBinding by viewBinding()

    private var lastLogin: String = ""
    private var lastPassword: String = ""

    private var errorTimer: Timer? = null
    private var captchaInputLayout: TextInputLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (parentFragment?.parentFragment as? MainFragment)?.bottomBar?.isVisible = false

        prepareViews()

        binding.loginInput.clearFocus()

        setFragmentResultListener("validation") { _, bundle ->
            lifecycleScope.launch { viewModel.getValidatedData(bundle) }
        }

//        showCaptchaDialog(
//            "https://www.vets4pets.com/syssiteassets/species/cat/kitten/tiny-kitten-in-field.jpg?width=1040",
//            ""
//        )
    }

    override fun onEvent(event: VKEvent) {
        super.onEvent(event)

        when (event) {
            is ShowError -> showErrorSnackbar(event.errorDescription)
            is CaptchaRequired -> showCaptchaDialog(event.captcha.first, event.captcha.second)
            is ValidationRequired -> goToValidation()
            is SuccessAuth -> goToMain(event.haveAuthorized)
            StartProgressEvent -> onProgressStarted()
            StopProgressEvent -> onProgressStopped()
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
        prepareEmailEditText()
        preparePasswordEditText()
        prepareAuthButton()
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
        binding.auth.setOnClickListener { validateDataAndAuth() }
        binding.auth.setOnLongClickListener {
            validateDataAndAuth(BuildConfig.vkLogin to BuildConfig.vkPassword)
            true
        }
    }

    private fun validateDataAndAuth(data: Pair<String, String>? = null) {
        if (binding.progress.isVisible) return
        val loginString = data?.first ?: binding.loginInput.text.toString().trim()
        val passwordString = data?.second ?: binding.passwordInput.text.toString().trim()

        if (!validateInputData(loginString, passwordString)) return

        lastLogin = loginString
        lastPassword = passwordString

        KeyboardUtils.hideKeyboardFrom(requireView().findFocus())

        lifecycleScope.launch {
            viewModel.login(
                login = loginString,
                password = passwordString
            )
        }
    }

    // TODO: 7/27/2021 extract strings to resources
    private fun validateInputData(
        loginString: String?,
        passwordString: String?,
        captchaCode: String? = null
    ): Boolean {
        var isValidated = true

        if (loginString?.isEmpty() == true) {
            isValidated = false
            setError("Input login", binding.loginLayout)
        }

        if (passwordString?.isEmpty() == true) {
            isValidated = false
            setError("Input password", binding.passwordLayout)
        }

        if (captchaCode?.isEmpty() == true && captchaInputLayout != null) {
            isValidated = false
            setError("Input code", captchaInputLayout!!)
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

            lifecycleScope.launch {
                viewModel.login(
                    login = lastLogin,
                    password = lastPassword,
                    captcha = captchaSid to captchaCode
                )
            }
        }
        captchaBinding.cancel.setOnClickListener { dialog.dismiss() }
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

    private fun goToValidation() {
//        findNavController().navigate(
//            R.id.toValidation,
//            bundleOf("redirectUrl" to redirectUrl)
//        )
    }

    private fun goToMain(haveAuthorized: Boolean) {
        lifecycleScope.launch {
            if (haveAuthorized) delay(500)

            findNavController().navigate(R.id.toMain)
        }
    }

}