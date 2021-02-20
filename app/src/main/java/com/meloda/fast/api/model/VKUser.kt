package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "users")
open class VKUser(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    companion object {

        var friendsCount: Int = 0

        const val DEFAULT_FIELDS =
            "photo_50,photo_100,photo_200,status,screen_name,online,online_mobile,last_seen,verified,sex"

        val EMPTY: VKUser = object : VKUser() {
            override fun toString(): String {
                return "Unknown Unknown"
            }
        }

        fun isUserId(id: Int): Boolean {
            return id > 0 && id < 2000000000
        }


        @JvmStatic
        fun parse(array: JSONArray): ArrayList<VKUser> {
            val users = ArrayList<VKUser>()

            for (i in 0 until array.length()) {
                users.add(VKUser(array.optJSONObject(i)))
            }

            return users
        }
    }

    @PrimaryKey(autoGenerate = false)
    var userId = o.optInt("id", -1)
    var firstName: String = o.optString("first_name")
    var lastName: String = o.optString("last_name")
    var deactivated: String = o.optString("deactivated")
    var isClosed = o.optBoolean("is_closed")
    var isCanAccessClosed = o.optBoolean("can_access_closed")
    var sex = o.optInt("sex")
    var screenName: String = o.optString("screen_name")
    var photo50: String = o.optString("photo_50")
    var photo100: String = o.optString("photo_100")
    var photo200: String = o.optString("photo_200")
    var isOnline = o.optInt("online") == 1
    var isOnlineMobile = isOnline && o.optInt("online_mobile") == 1
    var status: String = o.optString("status")

    var lastSeen = 0
    var lastSeenPlatform = 0

    var isVerified = o.optInt("verified") == 1

    init {
        o.optJSONObject("last_seen")?.let {
            lastSeen = it.optInt("time")
            lastSeenPlatform = it.optInt("platform")
        }
    }

    override fun toString(): String {
        return "$firstName $lastName"
    }
}