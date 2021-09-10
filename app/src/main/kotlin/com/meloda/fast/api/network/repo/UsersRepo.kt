package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.BaseVkUser
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VKUrls
import com.meloda.fast.api.network.request.UsersGetRequest
import dagger.Component
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersRepo {

    @POST(VKUrls.Users.getById)
    suspend fun getById(@Body param: UsersGetRequest): Answer<ApiResponse<List<BaseVkUser>>>

}