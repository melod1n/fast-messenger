package com.meloda.fast.data.users

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.network.users.UsersUrls
import com.slack.eithernet.ApiResult
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UsersService {

    @FormUrlEncoded
    @POST(UsersUrls.GET_BY_ID)
    suspend fun getById(
        @FieldMap params: Map<String, String>?
    ): ApiResult<ApiResponse<List<VkUserData>>, RestApiError>
}
