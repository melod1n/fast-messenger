package com.meloda.fast.api.base

import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.VKException

data class ApiError(
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("error_msg")
    override var message: String
) : VKException(error = message, code = errorCode)
