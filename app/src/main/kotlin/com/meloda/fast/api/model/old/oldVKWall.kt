package com.meloda.fast.api.model.old

import org.json.JSONObject

class oldVKWall() : VKModel() { //https://vk.com/dev/objects/post

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.WALL_POST

    constructor(o: JSONObject) : this() {}

}