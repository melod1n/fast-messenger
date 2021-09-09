package com.meloda.fast.api.base

data class ApiResponse<T>(
    val error: ApiError? = null,
    val response: T? = null
) {
    val isSuccessful get() = error == null && response != null
}