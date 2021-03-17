package com.meloda.vksdk.model

import org.json.JSONObject

class VKAudioMessage() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.VOICE_MESSAGE

    var duration: Int = 0
    var waveform: ArrayList<Int> = arrayListOf()
    var linkOgg: String = ""
    var linkMp3: String = ""

    constructor(o: JSONObject) : this() {
        duration = o.optInt("duration")
        linkOgg = o.optString("link_ogg")
        linkMp3 = o.optString("link_mp3")

        o.optJSONArray("waveform")?.let {
            val waveform = ArrayList<Int>()
            for (i in 0 until it.length()) {
                waveform.add(it.optInt(i))
            }
            this.waveform = waveform
        }
    }
}