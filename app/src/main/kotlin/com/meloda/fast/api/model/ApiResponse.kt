package com.meloda.fast.api.model

data class ApiResponse<T> constructor(
    val isSuccessful: Boolean,
    val error: Error?,
    val response: T?
)

data class Error constructor(
    val code: Long,
    val message: String
)