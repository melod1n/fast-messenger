package dev.meloda.fast.network.service.friends

import dev.meloda.fast.model.api.responses.GetFriendsResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FriendsService {

    @FormUrlEncoded
    @POST(FriendsUrls.GET)
    suspend fun getFriends(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<GetFriendsResponse>, RestApiError>

    @FormUrlEncoded
    @POST(FriendsUrls.GET_ONLINE)
    suspend fun getOnlineFriends(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<List<Long>>, RestApiError>
}
