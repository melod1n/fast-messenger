package com.meloda.vksdk.model

import org.json.JSONObject

class VKGeolocation() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.GEOLOCATION

    constructor(o: JSONObject) : this() {}

}