package com.meloda.fast.api.model

import org.json.JSONObject

class VKAudio(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var id = o.optInt("id", -1)
    var ownerId = o.optInt("owner_id", -1)
    var artist: String = o.optString("artist")
    var title: String = o.optString("title")
    var duration = o.optInt("duration")
    var url: String = o.optString("url")
    var date = o.optInt("date")

}