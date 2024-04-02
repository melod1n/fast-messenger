package com.meloda.fast.api.network

import com.meloda.fast.api.base.ApiException

@Deprecated("Use eithernet library")
sealed class ApiAnswer<out R> {

    data class Success<out T>(val data: T) : ApiAnswer<T>()
    data class Error(val error: ApiException) : ApiAnswer<Nothing>()
}
