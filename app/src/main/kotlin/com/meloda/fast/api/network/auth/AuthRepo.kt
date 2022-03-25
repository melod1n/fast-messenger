package com.meloda.fast.api.network.auth

import com.meloda.fast.api.network.Answer
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AuthRepo {

    @GET(AuthUrls.DirectAuth)
    suspend fun auth(@QueryMap param: Map<String, String?>): Answer<AuthDirectResponse>

    @GET(AuthUrls.SendSms)
    suspend fun sendSms(@Query("sid") validationSid: String): Answer<SendSmsResponse>

}