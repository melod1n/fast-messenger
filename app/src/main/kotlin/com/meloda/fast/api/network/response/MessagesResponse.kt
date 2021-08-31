package com.meloda.fast.api.network.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

class MessagesResponse(
    val count: Int
) {
}

@Parcelize
data class GetConversationsResponse(val a: String) : Parcelable
// TODO: 7/12/2021 use hilt for this like in LIR and make simple conversations' screen
