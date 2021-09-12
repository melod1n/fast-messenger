package com.meloda.fast.api.base

import com.google.gson.annotations.SerializedName
import java.io.IOException

data class ApiError(
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("error_msg")
    override var message: String
) : IOException()
