package com.meloda.vksdk.model

import org.json.JSONObject
import java.util.*

class VKPhoto() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.PHOTO

    var id: Int = 0
    var albumId: Int = 0
    var ownerId: Int = 0
    var text: String = ""
    var date: Int = 0
    var width: Int = 0
    var height: Int = 0
    var sizes: ArrayList<VKPhotoSize>? = null

    constructor(o: JSONObject) : this() {
        id = o.optInt("id", -1)
        albumId = o.optInt("album_id", -1)
        ownerId = o.optInt("owner_id", -1)
        text = o.optString("text")
        date = o.optInt("date")
        width = o.optInt("width")
        height = o.optInt("height")

        o.optJSONArray("sizes")?.let {
            val sizes = ArrayList<VKPhotoSize>()
            for (i in 0 until it.length()) {
                sizes.add(VKPhotoSize(it.optJSONObject(i)))
            }
            this.sizes = sizes
        }
    }
}