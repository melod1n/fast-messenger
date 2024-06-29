package com.meloda.app.fast.model

sealed class BaseError {
    data object SessionExpired : BaseError()
}
