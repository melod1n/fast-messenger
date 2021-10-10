package com.meloda.fast.api.network.auth

import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VkUrls
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AuthRepo {

    @GET(VkUrls.Auth.DirectAuth)
    suspend fun auth(@QueryMap param: Map<String, String?>): Answer<ResponseAuthDirect>

    @GET(VkUrls.Auth.SendSms)
    suspend fun sendSms(@Query("sid") validationSid: String): Answer<ResponseSendSms>

}