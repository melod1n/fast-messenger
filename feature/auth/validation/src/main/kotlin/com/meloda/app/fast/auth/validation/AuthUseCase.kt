package com.meloda.app.fast.auth.validation

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.responses.ValidatePhoneResponse
import kotlinx.coroutines.flow.Flow

interface AuthUseCase {

    fun validatePhone(
        validationSid: String
    ): Flow<State<ValidatePhoneResponse>>
}
