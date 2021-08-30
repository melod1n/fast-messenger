package com.meloda.fast.api.model.request

import com.google.gson.annotations.SerializedName

class RequestMessagesGetConversations(
    @SerializedName("offset")
    private val offset: Int = 0,

    @SerializedName("count")
    private val count: Int = 0,

    //values = all, unread
    @SerializedName("filter")
    private val filter: String = "",

    @SerializedName("extended")
    private val extended: Boolean = false,

    @SerializedName("fields")
    private var fields: String = ""
)