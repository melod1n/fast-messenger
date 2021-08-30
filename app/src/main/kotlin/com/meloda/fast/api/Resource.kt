package com.meloda.fast.api

data class Resource<out T> constructor(
    val status: Status,
    val responseData: T?,
    val message: String?
) {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(responseData: T?): Resource<T> =
            Resource(Status.SUCCESS, responseData, null)

        fun <T> error(message: String?, responseBody: T? = null): Resource<T> =
            Resource(Status.ERROR, responseBody, message)

        fun <T> loading(responseData: T? = null): Resource<T> =
            Resource(Status.LOADING, responseData, null)
    }
}
