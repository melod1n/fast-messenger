package com.meloda.fast.api.network.ota

import com.meloda.fast.api.network.Answer
import com.meloda.fast.model.UpdateItem
import retrofit2.http.GET

interface OtaRepo {

    @GET(OtaUrls.GetUpdates)
    suspend fun getUpdates(): Answer<UpdateItem>

}