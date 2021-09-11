package com.meloda.fast.api.model.old

import org.json.JSONObject

class oldVKAudio() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.AUDIO

    var id: Int = 0
    var ownerId: Int = 0
    var artist: String = ""
    var title: String = ""
    var duration: Int = 0
    var url: String = ""
    var date: Int = 0

    constructor(o: JSONObject) : this() {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        artist = o.optString("artist")
        title = o.optString("title")
        duration = o.optInt("duration")
        url = o.optString("url")
        date = o.optInt("date")
    }

}