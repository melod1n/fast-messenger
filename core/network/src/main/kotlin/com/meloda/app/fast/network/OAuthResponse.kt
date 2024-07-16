package dev.meloda.fast.network

sealed interface OAuthResponse<out R : Any, out E : Any> {
    data class Success<out R : Any>(val response: R) : OAuthResponse<R, Nothing>
    data class Error<out E : Any>(val error: E?) : OAuthResponse<Nothing, E>
}
