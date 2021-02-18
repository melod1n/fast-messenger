package ru.melod1n.project.vkm.api.model

import org.json.JSONObject
import java.util.*

class VKSticker(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var productId = o.optInt("product_id", -1)
    var stickerId = o.optInt("sticker_id", -1)
    var images: ArrayList<Image>? = null

    init {
        o.optJSONArray("images")?.let {
            val images = ArrayList<Image>()
            for (i in 0 until it.length()) {
                images.add(Image(it.optJSONObject(i)))
            }
            this.images = images
        }
    }

    class Image(o: JSONObject) : VKModel() {

        var url: String = o.optString("url")
        var width = o.optInt("width")
        var height = o.optInt("height")

    }
}