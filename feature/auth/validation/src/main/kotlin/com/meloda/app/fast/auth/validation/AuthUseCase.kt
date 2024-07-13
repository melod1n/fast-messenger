package com.meloda.app.fast.auth.validation

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.responses.SendSmsResponse
import kotlinx.coroutines.flow.Flow

interface AuthUseCase {

    fun sendSms(
        validationSid: String
    ): Flow<State<SendSmsResponse>>
}
