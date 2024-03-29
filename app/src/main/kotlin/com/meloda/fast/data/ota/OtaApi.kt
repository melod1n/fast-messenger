package com.meloda.fast.data.ota

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.ota.OtaGetLatestReleaseResponse
import com.meloda.fast.api.network.ota.OtaUrls
import com.meloda.fast.model.UpdateActualUrl
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url

interface OtaApi {

    @GET(OtaUrls.GetActualUrl)
    suspend fun getActualUrl(): ApiAnswer<UpdateActualUrl>

    @GET
    suspend fun getLatestRelease(
        @Url url: String,
        @Query("productId") productId: Int = 28,
        @Query("branchId") branchId: Int = 10,
        @Header("Secret-Code") secretCode: String
    ): ApiAnswer<ApiResponse<OtaGetLatestReleaseResponse>>

}