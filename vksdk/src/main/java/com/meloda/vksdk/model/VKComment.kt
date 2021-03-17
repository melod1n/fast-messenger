package com.meloda.vksdk.model

import org.json.JSONObject

class VKComment() : VKModel() { //https://vk.com/dev/objects/comment

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.WALL_REPLY

    constructor(o: JSONObject) : this() {}

}