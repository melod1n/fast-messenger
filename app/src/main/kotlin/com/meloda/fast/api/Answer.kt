package com.meloda.fast.api

sealed class Answer<out R> {
    data class Success<out T>(val data: T) : Answer<T>()
    data class Error(val errorString: String) : Answer<Nothing>()
}