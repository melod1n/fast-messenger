package dev.meloda.fast.common.util

import java.net.URLEncoder

fun String.urlEncode(encoding: String = "utf-8"): String {
    return URLEncoder.encode(this, encoding)
}
