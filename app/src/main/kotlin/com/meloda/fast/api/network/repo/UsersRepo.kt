package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.BaseVkUser
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VkUrls
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UsersRepo {

    @FormUrlEncoded
    @POST(VkUrls.Users.GetById)
    suspend fun getById(
        @FieldMap params: Map<String, String>?
    ): Answer<ApiResponse<List<BaseVkUser>>>

}