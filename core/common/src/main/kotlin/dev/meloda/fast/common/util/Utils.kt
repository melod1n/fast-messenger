package dev.meloda.fast.common.util

import java.net.URLEncoder
import java.security.MessageDigest

fun String.urlEncode(encoding: String = "utf-8"): String {
    return URLEncoder.encode(this, encoding)
}

fun String.sha256() = this.hashString("SHA-256")

fun String.hashString(algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}
