package com.meloda.fast.modules.auth.model.domain.usecase

import com.meloda.fast.api.network.auth.SendSmsResponse
import com.meloda.fast.base.State
import kotlinx.coroutines.flow.Flow

interface AuthUseCase {

    fun sendSms(
        validationSid: String
    ): Flow<State<SendSmsResponse>>
}
