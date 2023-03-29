package com.meloda.fast.screens.twofa.presentation

import androidx.lifecycle.ViewModel
import com.meloda.fast.screens.twofa.model.TwoFaScreenState
import com.meloda.fast.screens.twofa.screen.TwoFaCoordinator
import com.meloda.fast.screens.twofa.screen.TwoFaResult
import com.meloda.fast.screens.twofa.validation.TwoFaValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


interface TwoFaViewModel {

    val screenState: StateFlow<TwoFaScreenState>

    val isNeedToShowCodeError: StateFlow<Boolean>

    fun onCodeInputChanged(newCode: String)

    fun onBackButtonClicked()
    fun onCancelButtonClicked()
    fun onRequestSmsButtonClicked()
    fun onTextFieldDoneClicked()
    fun onDoneButtonClicked()
}

class TwoFaViewModelImpl constructor(
    private val coordinator: TwoFaCoordinator,
    private val validator: TwoFaValidator
) : TwoFaViewModel, ViewModel() {

    override val screenState = MutableStateFlow(TwoFaScreenState.EMPTY)

    override val isNeedToShowCodeError = MutableStateFlow(false)

    override fun onCodeInputChanged(newCode: String) {
        val newState = screenState.value.copy(twoFaCode = newCode.trim())
        screenState.update { newState }
        processValidation()
    }

    override fun onBackButtonClicked() {
        onCancelButtonClicked()
    }

    override fun onCancelButtonClicked() {
        clearState()
        coordinator.finishWithResult(TwoFaResult.Cancelled)
    }

    override fun onRequestSmsButtonClicked() {
    }

    override fun onTextFieldDoneClicked() {
        processValidation()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        val twoFaCode = screenState.value.twoFaCode

        clearState()
        coordinator.finishWithResult(TwoFaResult.Success(code = twoFaCode))
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()
        isNeedToShowCodeError.tryEmit(!isValid)
        return isValid
    }

    private fun clearState() {
        screenState.tryEmit(TwoFaScreenState.EMPTY)
        isNeedToShowCodeError.tryEmit(false)
    }
}
