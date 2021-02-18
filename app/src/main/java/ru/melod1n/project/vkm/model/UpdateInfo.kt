package ru.melod1n.project.vkm.model

import org.json.JSONObject

class UpdateInfo() {

    var version: String = ""
    var code: Int = 0
    var changelog: String = ""
    var downloadLink: String = ""
    var date: Int = 0

    constructor(o: JSONObject): this() {
        version = o.optString("lastVersionName")
        code = o.optInt("lastVersionCode")
        changelog = o.optString("changelog")
        downloadLink = o.optString("downloadLink")
        date = o.optInt("buildDate")
    }

}