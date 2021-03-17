package com.meloda.vksdk.model

import org.json.JSONObject
import java.util.*

class VKSticker() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.STICKER

    var productId: Int = 0
    var stickerId: Int = 0
    var images: ArrayList<Image>? = null

    constructor(o: JSONObject) : this() {
        productId = o.optInt("product_id", -1)
        stickerId = o.optInt("sticker_id", -1)

        o.optJSONArray("images")?.let {
            val images = ArrayList<Image>()
            for (i in 0 until it.length()) {
                images.add(Image(it.optJSONObject(i)))
            }
            this.images = images
        }
    }

    class Image(o: JSONObject) : VKModel() {

        companion object {
            const val serialVersionUID: Long = 1L
        }

        override val attachmentType = VKAttachments.Type.NONE

        var url: String = o.optString("url")
        var width = o.optInt("width")
        var height = o.optInt("height")

    }
}