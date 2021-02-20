package com.meloda.fast.api.model

import org.json.JSONObject
import java.util.*

class VKPhoto(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var id = o.optInt("id", -1)
    var albumId = o.optInt("album_id", -1)
    var ownerId = o.optInt("owner_id", -1)
    var text: String = o.optString("text")
    var date = o.optInt("date")
    var width = o.optInt("width")
    var height = o.optInt("height")
    var sizes: ArrayList<VKPhotoSize>? = null

    init {
        o.optJSONArray("sizes")?.let {
            val sizes = ArrayList<VKPhotoSize>()
            for (i in 0 until it.length()) {
                sizes.add(VKPhotoSize(it.optJSONObject(i)))
            }
            this.sizes = sizes
        }
    }
}