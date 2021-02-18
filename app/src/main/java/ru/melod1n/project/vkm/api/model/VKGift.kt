package ru.melod1n.project.vkm.api.model

import org.json.JSONObject

class VKGift(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var id = o.optInt("id", -1)
    var thumb256: String = o.optString("thumb_256")
    var thumb96: String = o.optString("thumb_96")
    var thumb48: String = o.optString("thumb_48")

}