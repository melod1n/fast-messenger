package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.BaseVkUser
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VKUrls
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface UsersRepo {

    @GET(VKUrls.Users.getById)
    suspend fun getById(@QueryMap params: Map<String, String>): Answer<ApiResponse<List<BaseVkUser>>>

}