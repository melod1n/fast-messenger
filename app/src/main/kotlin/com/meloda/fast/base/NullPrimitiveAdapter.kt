package com.meloda.fast.base

import com.squareup.moshi.FromJson

internal class NullPrimitiveAdapter {

    @FromJson
    fun stringFromJson(value: String?): String {
        return value.orEmpty()
    }

    @FromJson
    fun intFromJson(value: Int?): Int {
        return value ?: 0
    }

    @FromJson
    fun booleanFromJson(value: Boolean?): Boolean {
        return value ?: false
    }

    fun floatFromJson(value: Float?): Float {
        return value ?: 0f
    }

    @FromJson
    fun doubleFromJson(value: Double?): Double {
        return value ?: 0.0
    }
}
