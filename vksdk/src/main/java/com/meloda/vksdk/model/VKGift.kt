package com.meloda.vksdk.model

import org.json.JSONObject

class VKGift() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.GIFT

    var id: Int = 0
    var thumb256: String = ""
    var thumb96: String = ""
    var thumb48: String = ""

    constructor(o: JSONObject) : this() {
        id = o.optInt("id", -1)
        thumb256 = o.optString("thumb_256")
        thumb96 = o.optString("thumb_96")
        thumb48 = o.optString("thumb_48")
    }

}