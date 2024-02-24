package com.meloda.fast.data.users

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.VkUserData
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.users.UsersUrls
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UsersApi {

    @FormUrlEncoded
    @POST(UsersUrls.GetById)
    suspend fun getById(
        @FieldMap params: Map<String, String>?
    ): ApiAnswer<ApiResponse<List<VkUserData>>>

}
