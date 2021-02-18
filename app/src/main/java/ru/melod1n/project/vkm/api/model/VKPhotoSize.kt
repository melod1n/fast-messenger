package ru.melod1n.project.vkm.api.model

import org.json.JSONObject

class VKPhotoSize(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var type: String = o.optString("type")
    var url: String = o.optString("url")
    var height = o.optInt("height")
    var width = o.optInt("width")

}