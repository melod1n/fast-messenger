package dev.meloda.fast.data

import dev.meloda.fast.model.BaseError
import dev.meloda.fast.network.VkErrorCode

object VkUtils {

    fun parseError(error: State.Error): BaseError? {
        return when (error) {
            is State.Error.ApiError -> {
                when (error.errorCode) {
                    VkErrorCode.USER_AUTHORIZATION_FAILED -> {
                        if (error.errorMessage.startsWith(
                                "User authorization failed: user is blocked."
                            )
                        ) {
                            BaseError.AccountBlocked
                        } else {
                            BaseError.SessionExpired
                        }
                    }

                    else -> BaseError.SimpleError(message = error.errorMessage)
                }
            }

            State.Error.ConnectionError -> BaseError.ConnectionError
            State.Error.InternalError -> BaseError.InternalError
            State.Error.UnknownError -> BaseError.UnknownError

            else -> null
        }
    }
}
