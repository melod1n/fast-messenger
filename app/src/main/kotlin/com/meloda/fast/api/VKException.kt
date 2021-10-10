package com.meloda.fast.api

import org.json.JSONObject
import java.io.IOException

open class VKException(
    var url: String = "",
    var code: Int = -1,
    var description: String = "",
    var error: String
) : IOException(description) {

    // TODO: 10-Oct-21 remove this
    var json: JSONObject? = null

    override fun toString(): String {
        return "error: $error; description: $description;"
    }

}