package com.meloda.fast.api.network.longpoll

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LongPollGetUpdatesRequest(
    val act: String = "a_check",
    val key: String,
    val ts: Int,
    val wait: Int,
    val mode: Int,
    val version: Int
) : Parcelable {

    val map
        get() = mutableMapOf(
            "act" to act,
            "key" to key,
            "ts" to ts.toString(),
            "wait" to wait.toString(),
            "mode" to mode.toString(),
            "version" to version.toString()
        )
}
