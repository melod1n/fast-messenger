package com.meloda.vksdk.method

class UserMethodSetter(name: String) : MethodSetter(name) {

    fun extended(extended: Boolean): UserMethodSetter {
        put("extended", extended)
        return this
    }

    fun type(type: String): UserMethodSetter {
        put("type", type)
        return this
    }

    fun comment(comment: String): UserMethodSetter {
        put("comment", comment)
        return this
    }

    fun latitude(latitude: Float): UserMethodSetter {
        put("latitude", latitude)
        return this
    }

    fun longitude(longitude: Float): UserMethodSetter {
        put("longitude", longitude)
        return this
    }

    fun accuracy(accuracy: Int): UserMethodSetter {
        put("accuracy", accuracy)
        return this
    }

    fun timeout(timeout: Int): UserMethodSetter {
        put("timeout", timeout)
        return this
    }

    fun radius(radius: Int): UserMethodSetter {
        put("radius", radius)
        return this
    }
}