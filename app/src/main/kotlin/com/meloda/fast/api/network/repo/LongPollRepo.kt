package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.Answer
import org.json.JSONObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface LongPollRepo {

    @GET("https://{serverUrl}")
    suspend fun getResponse(
        @Path("serverUrl") serverUrl: String,
        @QueryMap params: Map<String, String>
    ): Answer<ApiResponse<JSONObject>>

}