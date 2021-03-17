package com.meloda.vksdk.model

import org.json.JSONObject

class VKGraffiti() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.GRAFFITI

    var id: Int = 0
    var ownerId: Int = 0
    var url: String = ""
    var width: Int = 0
    var height: Int = 0
    var accessKey: String = ""

    constructor(o: JSONObject) : this() {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        url = o.optString("url")
        width = o.optInt("width")
        height = o.optInt("height")
        accessKey = o.optString("access_key")
    }

}