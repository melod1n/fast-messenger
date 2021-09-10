package com.meloda.fast.api.model.old

import org.json.JSONObject

class VKPhotoSize(o: JSONObject) : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.NONE

    var type: String = o.optString("type")
    var url: String = o.optString("url")
    var height: Int = o.optInt("height")
    var width: Int = o.optInt("width")

}