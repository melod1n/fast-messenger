package com.meloda.fast.api.network

import com.meloda.fast.api.base.ApiError


sealed class ApiAnswer<out R> {

    data class Success<out T>(val data: T) : ApiAnswer<T>()
    data class Error(val error: ApiError) : ApiAnswer<Nothing>()
}
