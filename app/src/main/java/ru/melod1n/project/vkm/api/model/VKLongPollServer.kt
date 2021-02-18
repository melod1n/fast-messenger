package ru.melod1n.project.vkm.api.model

import org.json.JSONObject

class VKLongPollServer(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var key: String = o.optString("key")
    var server = o.optString("server").replace("\\", "")
    var ts: Long = o.optLong("ts")

}