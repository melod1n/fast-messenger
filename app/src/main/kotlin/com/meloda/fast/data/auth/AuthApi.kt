package com.meloda.fast.data.auth

import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.auth.AuthDirectResponse
import com.meloda.fast.api.network.auth.AuthUrls
import com.meloda.fast.api.network.auth.SendSmsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AuthApi {

    @GET(AuthUrls.DirectAuth)
    suspend fun auth(@QueryMap param: Map<String, String?>): ApiAnswer<AuthDirectResponse>

    @GET(AuthUrls.SendSms)
    suspend fun sendSms(@Query("sid") validationSid: String): ApiAnswer<SendSmsResponse>

}