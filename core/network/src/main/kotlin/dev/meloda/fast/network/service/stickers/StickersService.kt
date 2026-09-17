package dev.meloda.fast.network.service.stickers

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.StoreGetProductsResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface StickersService {

    @FormUrlEncoded
    @POST(StickersUrls.GET_PRODUCTS)
    suspend fun getProducts(
        @FieldMap map: Map<String, String>
    ): ApiResult<ApiResponse<StoreGetProductsResponse>, RestApiError>
}
