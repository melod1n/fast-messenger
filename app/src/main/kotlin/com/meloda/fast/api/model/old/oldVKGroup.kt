package com.meloda.fast.api.model.old

import org.json.JSONArray
import org.json.JSONObject

open class oldVKGroup() : VKModel() {

    override val attachmentType = VKAttachments.Type.NONE

    companion object {

        const val serialVersionUID: Long = 1L

        fun parse(array: JSONArray): ArrayList<oldVKGroup> {
            val groups = ArrayList<oldVKGroup>()

            for (i in 0 until array.length()) {
                groups.add(oldVKGroup(array.optJSONObject(i)))
            }
            return groups
        }
    }

    var id: Int = 0
    var name: String = ""
    var screenName: String = ""
    var isClosed: Boolean = false
    var deactivated: String = ""
    var type: Type = Type.NULL
    var photo50: String = ""
    var photo100: String = ""
    var photo200: String = ""

    constructor(o: JSONObject) : this() {
        id = o.optInt("id", -1)
        name = o.optString("name")
        screenName = o.optString("screen_name")
        isClosed = o.optInt("is_closed") == 1
        deactivated = o.optString("deactivated")
        type = Type.fromString(o.optString("type"))
        photo50 = o.optString("photo_50")
        photo100 = o.optString("photo_100")
        photo200 = o.optString("photo_200")
    }

    enum class Type(val value: String) {
        NULL("null"),
        GROUP("group"),
        PAGE("page"),
        EVENT("event");

        companion object {
            fun fromString(value: String) = values().first { it.value == value }
        }
    }
}