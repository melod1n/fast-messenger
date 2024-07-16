package dev.meloda.fast.network

import java.lang.reflect.Type

interface JsonConverter {

    fun fromJson(clazz: Class<*>, jsonString: String): Any?

    fun fromJson(type: Type, jsonString: String): Any?
}
