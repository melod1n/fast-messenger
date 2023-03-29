package com.meloda.fast.screens.login

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.widget.doAfterTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.viewmodel.ViewModelUtils
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.databinding.DialogFastLoginBinding
import com.meloda.fast.databinding.FragmentLoginBinding
import com.meloda.fast.ext.*
import com.meloda.fast.model.base.Text
import com.meloda.fast.screens.login.model.LoginScreenState
import com.meloda.fast.util.ViewUtils.showDialog
import dev.chrisbanes.insetter.applyInsetter
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by activityViewModel<LoginViewModelImpl>()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareView()
        listenViewModel()
    }

    private fun handleEvent(event: VkEvent) {
        ViewModelUtils.parseEvent(this, event)
    }

    private fun prepareView() {
        applyInsets()
        prepareFields()
        prepareAuthButton()
    }

    private fun applyInsets() {
        binding.root.applyInsetter {
            type(statusBars = true) { padding() }
        }

        binding.signIn.applyInsetter {
            type(navigationBars = true) { margin() }
        }
    }

    private fun prepareFields() {
        binding.loginContainer.clearTextOnErrorIconClick(binding.login)
        binding.passwordContainer.clearTextOnErrorIconClick(binding.password)

        binding.login.clearFocus()

        binding.login.doAfterTextChanged { editable ->
            viewModel.onLoginInputChanged(editable?.toString().orEmpty())
        }
        binding.password.doAfterTextChanged { editable ->
            viewModel.onPasswordInputChanged(editable?.toString().orEmpty())
        }

        binding.password.onDone {
            binding.password.hideKeyboard()
            binding.signIn.performClick()
        }

        val roundedCorners = 10.dpToPx().toFloat()
        val onFocusChangedAction: (v: View, hasFocus: Boolean) -> Unit = { v, hasFocus ->
            applyFieldFocusChange(v, hasFocus, roundedCorners)
        }

        binding.login.setOnFocusChangeListener(onFocusChangedAction::invoke)
        binding.password.setOnFocusChangeListener(onFocusChangedAction::invoke)
    }

    private fun applyFieldFocusChange(v: View, hasFocus: Boolean, roundedCorners: Float) {
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

    private fun prepareAuthButton() {
        binding.signIn.setOnClickListener {
            viewModel.onSignInButtonClicked()
        }
        binding.signIn.setOnLongClickListener {
            viewModel.onSignInButtonLongClicked()
            true
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

    private fun showErrorDialog() {
        requireContext().showDialog(
            title = Text.Resource(R.string.title_error),
            message = Text.Simple(viewModel.screenState.value.error.orEmpty()),
            positiveText = Text.Resource(R.string.ok),
            onDismissAction = viewModel::onErrorDialogDismissed
        )
    }

    private fun listenViewModel() = with(viewModel) {
        events.listenValue(::handleEvent)
        screenState.listenValue(::handleScreenState)
        isNeedToShowLoginError.listenValue(::handleLoginErrorShow)
        isNeedToShowPasswordError.listenValue(::handlePasswordErrorShow)
        isNeedToShowErrorDialog.listenValue(::handleErrorAlertShow)
        isNeedToShowFastLoginDialog.listenValue(::handleFastLoginAlertShow)
    }

    private fun handleScreenState(state: LoginScreenState) {
        state.login.let(binding.login::updateTextIfDiffer)
        state.password.let(binding.password::updateTextIfDiffer)
    }

    private fun handleLoginErrorShow(isNeedToShow: Boolean) {
        binding.loginContainer.toggleError(loginErrorText, isNeedToShow)
    }

    private fun handlePasswordErrorShow(isNeedToShow: Boolean) {
        binding.passwordContainer.toggleError(passwordErrorText, isNeedToShow)
    }

    private fun handleErrorAlertShow(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showErrorDialog()
        }
    }

    private fun handleFastLoginAlertShow(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showFastLoginDialog()
        }
    }

    companion object {

        private const val EDIT_TEXT_ANIMATION_DURATION = 250L

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
