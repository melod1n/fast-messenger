package com.meloda.app.fast.data.api.auth

import com.meloda.app.fast.model.api.responses.SendSmsResponse

interface AuthRepository {

    suspend fun sendSms(
        validationSid: String
    ): SendSmsResponse
}
