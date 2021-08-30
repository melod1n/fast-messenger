package com.meloda.fast.model

import org.json.JSONObject

class NewUpdateInfo() {

    var id: Int = 0
    var version: String = ""
    var code: Int = 0
    var time: Int = 0
    var changelog: String = ""

//    var branchId: Int = 0
//    var branchName: String = ""

    var downloadLink: String = ""

//    var state: Boolean = true

    constructor(o: JSONObject) : this() {
        id = o.optInt("id")
        version = o.optString("version")
        code = o.optInt("code")
        time = o.optInt("time")
        changelog = o.optString("changelog")

        downloadLink = o.optString("download")
    }

}