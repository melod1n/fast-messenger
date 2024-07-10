package com.meloda.app.fast.network.service.friends

import com.meloda.app.fast.model.api.responses.GetFriendsResponse
import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
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
    ): ApiResult<ApiResponse<List<Int>>, RestApiError>
}
