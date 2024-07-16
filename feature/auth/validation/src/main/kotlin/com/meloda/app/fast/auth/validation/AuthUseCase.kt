package dev.meloda.fast.auth.validation

import dev.meloda.fast.data.State
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import kotlinx.coroutines.flow.Flow

interface AuthUseCase {

    fun validatePhone(
        validationSid: String
    ): Flow<State<ValidatePhoneResponse>>
}
