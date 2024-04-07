package com.meloda.fast.base.util

import com.meloda.fast.base.JsonConverter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

internal class MoshiConverter(private val moshi: Moshi) : JsonConverter {

    @kotlin.jvm.Throws(RuntimeException::class)
    override fun fromJson(clazz: Class<*>, jsonString: String): Any? {
        return moshi.adapter(clazz).fromJson(jsonString)
    }

    @kotlin.jvm.Throws(RuntimeException::class)
    override fun fromJson(type: Type, jsonString: String): Any? {
        return moshi.adapter<Any>(type).fromJson(jsonString)
    }
}
