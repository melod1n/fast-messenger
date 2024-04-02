package com.meloda.fast.api.network

import com.slack.eithernet.ApiException

@Deprecated("Use eithernet library")
sealed class ApiAnswer<out R> {

    data class Success<out T>(val data: T) : ApiAnswer<T>()
    data class Error(val error: ApiException) : ApiAnswer<Nothing>()
}
