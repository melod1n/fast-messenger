package com.meloda.fast.api.model.old

import org.json.JSONArray
import org.json.JSONObject

open class oldVKUser() : VKModel() {

    override val attachmentType = VKAttachments.Type.NONE

    companion object {
        const val serialVersionUID: Long = 1L

        var friendsCount: Int = 0

        fun parse(array: JSONArray): ArrayList<oldVKUser> {
            val users = ArrayList<oldVKUser>()

            for (i in 0 until array.length()) {
                users.add(oldVKUser(array.optJSONObject(i)))
            }

            return users
        }
    }

    var sortId: Int = 0

    var userId: Int = 0
    var firstName: String = ""
    var lastName: String = ""
    var deactivated: String = ""
    var isClosed: Boolean = false
    var isCanAccessClosed: Boolean = true
    var sex: Int = 0
    var screenName: String = ""
    var photo50: String = ""
    var photo100: String = ""
    var photo200: String = ""
    var isOnline: Boolean = false
    var isOnlineMobile: Boolean = false
    var status: String = ""

    var lastSeen: Int = 0
    var lastSeenPlatform: Int = 0

    var isVerified: Boolean = false

    constructor(o: JSONObject) : this() {
        sortId = 0
        userId = o.optInt("id", -1)
        firstName = o.optString("first_name")
        lastName = o.optString("last_name")
        deactivated = o.optString("deactivated", "")
        isClosed = o.optBoolean("is_closed")
        isCanAccessClosed = o.optBoolean("can_access_closed")
        sex = o.optInt("sex")
        screenName = o.optString("screen_name")
        photo50 = o.optString("photo_50")
        photo100 = o.optString("photo_100")
        photo200 = o.optString("photo_200")
        isOnline = o.optInt("online") == 1
        isOnlineMobile = isOnline && o.optInt("online_mobile") == 1
        status = o.optString("status")
        lastSeen = 0
        lastSeenPlatform = 0
        isVerified = o.optInt("verified") == 1

        o.optJSONObject("last_seen")?.let {
            lastSeen = it.optInt("time")
            lastSeenPlatform = it.optInt("platform")
        }

    }

    fun isDeactivated() = deactivated.isNotEmpty()

    override fun toString(): String {
        return "$firstName $lastName"
    }
}