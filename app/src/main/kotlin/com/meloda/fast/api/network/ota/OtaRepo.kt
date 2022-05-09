package com.meloda.fast.api.network.ota

import com.meloda.fast.BuildConfig
import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.Answer
import com.meloda.fast.model.UpdateActualUrl
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface OtaRepo {

    @GET(OtaUrls.GetActualUrl)
    suspend fun getActualUrl(): Answer<UpdateActualUrl>

    @GET
    suspend fun getLatestRelease(
        @Url url: String,
        @Query("productId") productId: Int = 28,
        @Query("branchId") branchId: Int = 10,
        @Query("secretCode") secretCode: String = BuildConfig.otaSecretCode
    ): Answer<ApiResponse<OtaGetLatestReleaseResponse>>

    @GET
    suspend fun downloadRelease(
        @Url url: String,
        @Query("secretCode") secretCode: String = BuildConfig.otaSecretCode
    ): Response<ResponseBody>

}