package com.meloda.fast.api.model

import org.json.JSONObject

class VKGraffiti(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var id = o.optInt("id", -1)
    var ownerId = o.optInt("owner_id", -1)
    var url: String = o.optString("url")
    var width = o.optInt("width")
    var height = o.optInt("height")
    var accessKey: String = o.optString("access_key")

}