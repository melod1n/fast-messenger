package com.meloda.fast.api.model.old

import org.json.JSONObject

class oldVKComment() : VKModel() { //https://vk.com/dev/objects/comment

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.WALL_REPLY

    constructor(o: JSONObject) : this() {}

}