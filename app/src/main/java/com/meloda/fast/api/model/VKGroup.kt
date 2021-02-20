package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "groups")
open class VKGroup(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    companion object {
        const val DEFAULT_FIELDS = "description,members_count,counters,status,verified"

        fun isGroupId(id: Int): Boolean {
            return id < 0
        }

        val EMPTY: VKGroup = object : VKGroup() {
            init {
                name = "Unknown"
            }
        }

        fun parse(array: JSONArray): ArrayList<VKGroup> {
            val groups = ArrayList<VKGroup>()

            for (i in 0 until array.length()) {
                groups.add(VKGroup(array.optJSONObject(i)))
            }
            return groups
        }
    }

    @PrimaryKey(autoGenerate = false)
    var groupId = o.optInt("id", -1)
    var name: String = o.optString("name")
    var screenName: String = o.optString("screen_name")
    var isClosed = o.optInt("is_closed") == 1
    var deactivated: String = o.optString("deactivated")
    var type: String = o.optString("type")
    var photo50: String = o.optString("photo_50")
    var photo100: String = o.optString("photo_100")
    var photo200: String = o.optString("photo_200")
}