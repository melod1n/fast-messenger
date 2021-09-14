package com.meloda.fast.api.network.repo

import com.meloda.fast.api.network.VKUrls
import com.meloda.fast.api.model.response.ResponseAuthDirect
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.model.response.ResponseSendSms
import retrofit2.http.*

interface AuthRepo {

    @GET(VKUrls.Auth.DirectAuth)
    suspend fun auth(@QueryMap param: Map<String, String?>): Answer<ResponseAuthDirect>

    @GET(VKUrls.Auth.SendSms)
    suspend fun sendSms(@Query("sid") validationSid: String): Answer<ResponseSendSms>

}