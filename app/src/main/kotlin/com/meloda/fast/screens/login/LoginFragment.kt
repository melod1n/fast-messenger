package com.meloda.fast.screens.login

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.viewmodel.CaptchaRequiredEvent
import com.meloda.fast.base.viewmodel.ValidationRequiredEvent
import com.meloda.fast.base.viewmodel.ViewModelUtils
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.DialogCaptchaBinding
import com.meloda.fast.databinding.DialogFastLoginBinding
import com.meloda.fast.databinding.DialogValidationBinding
import com.meloda.fast.databinding.FragmentLoginBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.color
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.hideKeyboard
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.onDone
import com.meloda.fast.ext.selectLast
import com.meloda.fast.ext.string
import com.meloda.fast.ext.toast
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.trimmedText
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.ViewUtils.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: ILoginViewModel by viewModels<LoginViewModel>()
    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val loginErrorText by lazy {
        string(R.string.input_login_hint)
    }
    private val passwordErrorText by lazy {
        string(R.string.input_password_hint)
    }
    private val codeErrorText by lazy {
        string(R.string.input_code_hint)
    }

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
        listenViewModel()
    }

    private fun onEvent(event: VkEvent) {
        ViewModelUtils.parseEvent(this, event)

        when (event) {
            is CaptchaRequiredEvent -> viewModel::onCaptchaEventReceived
            is ValidationRequiredEvent -> viewModel::onValidationEventReceived
        }
    }

    override fun toggleProgress(isProgressing: Boolean) {
        binding.progressBar.toggleVisibility(isProgressing)

        val newScale = if (binding.signIn.isVisible) 0F else 1F

        ValueAnimator.ofFloat(binding.signIn.scaleX, newScale).apply {
            duration = FAB_ANIMATION_DURATION
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
        prepareAuthButton()
        prepareCrashManagerCheckbox()
    }

    private fun applyInsets() {
        binding.root.applyInsetter {
            type(ime = true, statusBars = true) { padding() }
        }
    }

    private fun prepareFields() {
        binding.login.clearFocus()

        binding.login.doAfterTextChanged { editable ->
            viewModel.onLoginInputChanged(editable?.toString().orEmpty())
        }
        binding.password.doAfterTextChanged { editable ->
            viewModel.onPasswordInputChanged(editable?.toString().orEmpty())
        }

        binding.password.typeface = Typeface.DEFAULT
        binding.passwordContainer.endIconMode = TextInputLayout.END_ICON_NONE

        binding.password.onDone {
            binding.password.hideKeyboard()
            binding.signIn.performClick()
        }

        val roundedCorners = 10.dpToPx().toFloat()
        val onFocusChangedAction: (v: View, hasFocus: Boolean) -> Unit = { v, hasFocus ->
            val inputLayout = v.parent.parent as? TextInputLayout
                ?: throw NullPointerException("Something in layout was changed")
            val cornerRadiusToSet = if (hasFocus) 0F else roundedCorners

            if (inputLayout.boxCornerRadiusBottomEnd != cornerRadiusToSet) {
                ValueAnimator.ofFloat(
                    inputLayout.boxCornerRadiusBottomEnd,
                    cornerRadiusToSet
                ).apply {
                    duration = EDIT_TEXT_ANIMATION_DURATION
                    interpolator = LinearInterpolator()

                    addUpdateListener { animator ->
                        val value = animator.animatedValue as Float
                        inputLayout.setBoxCornerRadii(roundedCorners, roundedCorners, value, value)
                    }
                }.start()
            }
        }

        binding.login.setOnFocusChangeListener(onFocusChangedAction::invoke)
        binding.password.setOnFocusChangeListener(onFocusChangedAction::invoke)
    }

    private fun prepareAuthButton() {
        binding.signIn.setOnClickListener {
            viewModel.onSignInButtonClicked()
        }
        binding.signIn.setOnLongClickListener {
            viewModel.onSignInButtonLongClicked()
            true
        }
    }

    private fun prepareCrashManagerCheckbox() {
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

    private fun showFastLoginDialog() {
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

                    viewModel.onFastLoginDialogOkButtonClicked()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener { viewModel.onFastLoginDialogDismissed() }
            .show()
    }

    private fun showCaptchaDialog() {
        val captchaBinding = DialogCaptchaBinding.inflate(layoutInflater, null, false)

        viewModel.isNeedToShowCaptchaError.listenValue { needToShow ->
            captchaBinding.captchaLayout.error =
                if (needToShow) codeErrorText
                else null
        }

        captchaBinding.image.loadWithGlide {
            imageUrl = viewModel.formState.value.captchaImage
            crossFade = true
        }
        captchaBinding.image.shapeAppearanceModel =
            captchaBinding.image.shapeAppearanceModel.withCornerSize(16.dpToPx().toFloat())

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(captchaBinding.root)
            .setCancelable(false)
            .setTitle(R.string.input_captcha)
            .setOnDismissListener { viewModel.onCaptchaDialogDismissed() }

        val dialog = builder.show()

        captchaBinding.captchaInput.doAfterTextChanged { editable ->
            viewModel.onCaptchaCodeInputChanged(editable?.toString().orEmpty())
        }

        captchaBinding.ok.setOnClickListener {
            dialog.dismiss()
            viewModel.onCaptchaDialogOkButtonClicked()
        }
        captchaBinding.cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showValidationDialog() {
        val validationBinding = DialogValidationBinding.inflate(layoutInflater, null, false)

        viewModel.isNeedToShowValidationError.listenValue { needToShow ->
            validationBinding.codeLayout.error =
                if (needToShow) codeErrorText
                else null
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(validationBinding.root)
            .setCancelable(false)
            .setTitle(R.string.input_validation_code)
            .setOnDismissListener { viewModel.onValidationDialogDismissed() }

        val dialog = builder.show()

        validationBinding.codeInput.doAfterTextChanged { editable ->
            viewModel.onValidationCodeInputChanged(editable?.toString().orEmpty())
        }

        validationBinding.ok.setOnClickListener {
            dialog.dismiss()
            viewModel.onValidationDialogOkButtonClicked()
        }
        validationBinding.cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showErrorDialog() {
        requireContext().showErrorDialog(
            message = viewModel.formState.value.error.orEmpty(),
            positiveText = string(R.string.ok),
            onDismissAction = viewModel::onErrorDialogDismissed
        )
    }

    private fun listenViewModel() {
        lifecycleScope.launch {
            viewModel.events.collect { onEvent(it) }
        }

        viewModel.isNeedToShowLoginError.listenValue { needToShow ->
            binding.loginContainer.error = if (needToShow) loginErrorText else null
        }
        viewModel.isNeedToShowPasswordError.listenValue { needToShow ->
            binding.passwordContainer.error = if (needToShow) passwordErrorText else null
        }
        viewModel.isNeedToShowErrorDialog.listenValue { needToShow ->
            if (needToShow) {
                showErrorDialog()
            }
        }

        viewModel.isNeedToShowCaptchaDialog.listenValue { needToShow ->
            if (needToShow) {
                showCaptchaDialog()
            }
        }
        viewModel.isNeedToShowValidationDialog.listenValue { needToShow ->
            if (needToShow) {
                showValidationDialog()
            }
        }
        viewModel.isNeedToShowValidationToast.listenValue { needToShow ->
            if (needToShow) {
                string(R.string.validation_required).toast()
                viewModel.onValidationToastShown()
            }
        }
        viewModel.isNeedToShowFastLoginDialog.listenValue { needToShow ->
            if (needToShow) {
                showFastLoginDialog()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        requireActivity().window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }

    companion object {

        private const val FAB_ANIMATION_DURATION = 100L

        private const val EDIT_TEXT_ANIMATION_DURATION = 100L

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
