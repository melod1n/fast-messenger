package com.meloda.fast.api

import org.json.JSONObject
import java.io.IOException

class VKException(var url: String = "", var description: String = "", var error: String) :
    IOException(description) {

    var captcha: Pair<String, String>? = null
    var redirectUri: String? = null

    var validationSid: String? = null

    var json: JSONObject? = null

    override fun toString(): String {
        return "url: $url;\n\nerror: $error; description: $description;"
    }

}