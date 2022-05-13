package com.meloda.fast.api.network.longpoll

import com.google.gson.JsonObject
import com.meloda.fast.api.network.ApiAnswer
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface LongPollRepo {

    @GET
    suspend fun getResponse(
        @Url serverUrl: String,
        @QueryMap params: Map<String, String>
    ): ApiAnswer<JsonObject>

}