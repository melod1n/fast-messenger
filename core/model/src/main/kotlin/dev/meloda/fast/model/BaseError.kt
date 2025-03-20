package dev.meloda.fast.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class BaseError {

    data object SessionExpired : BaseError()

    data class SimpleError(val message: String) : BaseError()
}
