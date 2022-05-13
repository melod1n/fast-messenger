package com.meloda.fast.api.network.users

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.BaseVkUser
import com.meloda.fast.api.network.ApiAnswer
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UsersRepo {

    @FormUrlEncoded
    @POST(UsersUrls.GetById)
    suspend fun getById(
        @FieldMap params: Map<String, String>?
    ): ApiAnswer<ApiResponse<List<BaseVkUser>>>

}