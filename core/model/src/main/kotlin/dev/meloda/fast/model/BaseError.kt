package dev.meloda.fast.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class BaseError {

    data object SessionExpired : BaseError()
    data object AccountBlocked : BaseError()
    data object ConnectionError : BaseError()
    data object InternalError : BaseError()
    data object UnknownError : BaseError()

    data class SimpleError(val message: String) : BaseError()
}
