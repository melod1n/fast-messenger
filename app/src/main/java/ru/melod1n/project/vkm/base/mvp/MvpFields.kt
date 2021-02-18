package ru.melod1n.project.vkm.base.mvp

import androidx.collection.arrayMapOf
import java.util.*

@Suppress("UNCHECKED_CAST")
class MvpFields {
    private val fields = arrayMapOf<String, Any>()

    fun put(key: String, value: Any): MvpFields {
        fields[key] = value
        return this
    }

    operator fun <T> get(key: String): T {
        return fields[key] as T
    }

    fun getNonNull(key: String): Any {
        return fields[key]!!
    }

    fun getNonNull(`object`: Any): Any {
        return Objects.requireNonNull(`object`)
    }

    fun getFields(): Map<String, Any> {
        return fields
    }
}