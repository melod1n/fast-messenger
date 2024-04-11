package com.meloda.fast.base

import com.meloda.fast.api.network.BaseOAuthError

// TODO: 07/04/2024, Danil Nikolaev: map error state in string
/** Возможные состояния для одного потока данных в репозитории */
sealed class State<out T> {

    /**
     * Репозиторий еще ничего не делал
     */
    data object Idle : State<Nothing>()

    /**
     * Получение данных репозиторием закончилось успехом
     */
    data class Success<T>(val data: T) : State<T>()

    /**
     * Репозиторий в процессе получения данных
     */
    data object Loading : State<Nothing>()

    sealed class Error : State<Nothing>() {

        data class ApiError(
            val errorCode: Int,
            val errorMessage: String,
        ) : Error()

        data class OAuthError<out T: BaseOAuthError>(
            val error: T
        ) : Error()

        data object ConnectionError : Error()

        data object Unknown : Error()

        data object InternalError : Error()
    }

    fun isLoading(): Boolean = this is Loading

    companion object {

        val UNKNOWN_ERROR = Error.Unknown
    }
}
