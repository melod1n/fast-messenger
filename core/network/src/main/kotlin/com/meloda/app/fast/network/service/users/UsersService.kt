package com.meloda.app.fast.network.service.users

import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
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
