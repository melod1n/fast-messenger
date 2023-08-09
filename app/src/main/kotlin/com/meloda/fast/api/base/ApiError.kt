package com.meloda.fast.api.base

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okio.IOException

open class ApiError(
    @SerializedName("error", alternate = ["error_code"])
    val error: String? = null,
    @SerializedName("error_msg", alternate = ["error_description"])
    open val errorMessage: String? = null,
    @SerializedName("error_type")
    val errorType: String? = null,
    val throwable: Throwable? = null
) : IOException() {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}
