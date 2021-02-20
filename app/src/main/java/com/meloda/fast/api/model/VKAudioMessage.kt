package com.meloda.fast.api.model

import org.json.JSONObject

class VKAudioMessage(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var duration = o.optInt("duration")
    var waveform = ArrayList<Int>()
    var linkOgg: String = o.optString("link_ogg")
    var linkMp3: String = o.optString("link_mp3")

    init {
        o.optJSONArray("waveform")?.let {
            val waveform = ArrayList<Int>()
            for (i in 0 until it.length()) {
                waveform.add(it.optInt(i))
            }
            this.waveform = waveform
        }
    }
}