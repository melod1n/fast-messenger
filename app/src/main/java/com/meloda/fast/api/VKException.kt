package com.meloda.fast.api

import java.io.IOException

class VKException(var url: String, override var message: String, var code: Int) :
    IOException(message) {
    var captchaSid: String? = null
    var captchaImg: String? = null
    var redirectUri: String? = null

    override fun toString(): String {
        return "code: $code, message: $message"
    }

}