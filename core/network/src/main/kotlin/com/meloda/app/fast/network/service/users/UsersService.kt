package dev.meloda.fast.network.service.users

import dev.meloda.fast.model.api.data.VkUserData
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UsersService {

    @FormUrlEncoded
    @POST(UsersUrls.GET_BY_ID)
    suspend fun get(
        @FieldMap params: Map<String, String>?
    ): ApiResult<ApiResponse<List<VkUserData>>, RestApiError>
}
