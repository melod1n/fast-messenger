package com.meloda.fast.data.longpoll

import com.google.gson.JsonObject
import com.meloda.fast.api.network.ApiAnswer
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface LongPollApi {

    @GET
    suspend fun getResponse(
        @Url serverUrl: String,
        @QueryMap params: Map<String, String>
    ): ApiAnswer<JsonObject>

}