package com.meloda.vksdk.model

import org.json.JSONObject

class VKLongPollServer() : VKModel() {

    override val attachmentType = VKAttachments.Type.NONE

    var key: String = ""
    var server: String = ""
    var ts: Long = 0

    constructor(o: JSONObject) : this() {
        key = o.optString("key")
        server = o.optString("server").replace("\\", "")
        ts = o.optLong("ts")
    }

}