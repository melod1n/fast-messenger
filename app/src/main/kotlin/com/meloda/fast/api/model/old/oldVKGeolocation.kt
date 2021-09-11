package com.meloda.fast.api.model.old

import org.json.JSONObject

class oldVKGeolocation() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.GEOLOCATION

    constructor(o: JSONObject) : this() {}

}