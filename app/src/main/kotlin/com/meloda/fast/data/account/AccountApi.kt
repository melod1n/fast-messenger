package com.meloda.fast.data.account

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.account.AccountUrls
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface AccountApi {

    @GET(AccountUrls.SetOnline)
    suspend fun setOnline(@QueryMap params: Map<String, String>): ApiAnswer<ApiResponse<Any>>

    @POST(AccountUrls.SetOffline)
    suspend fun setOffline(@QueryMap params: Map<String, String>): ApiAnswer<ApiResponse<Any>>

}