package com.meloda.fast.api.model

import org.json.JSONObject

class VKVideo(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var id = o.optInt("id", -1)
    var ownerId = o.optInt("owner_id", -1)
    var title: String = o.optString("title")
    var description: String = o.optString("description")
    var duration = o.optInt("duration", -1)
    var photo130: String = o.optString("photo_130")
    var photo320: String = o.optString("photo_320")
    var photo640: String = o.optString("photo_640")
    var photo800: String = o.optString("photo_800")
    var photo1280: String = o.optString("photo_1280")
    var firstFrame130: String = o.optString("first_frame_130")
    var firstFrame320: String = o.optString("first_frame_320")
    var firstFrame640: String = o.optString("first_frame_640")
    var firstFrame800: String = o.optString("first_frame_800")
    var firstFrame1280: String = o.optString("first_frame_1280")
    var date = o.optInt("date")
    var views = o.optInt("views")
    var comments = o.optInt("comments")
    var player: String = o.optString("player")
    var isCanEdit = o.optInt("can_edit", 0) == 1
    var isCanAdd = o.optInt("can_add") == 1
    var isPrivate = o.optInt("is_private", 0) == 1
    var accessKey: String = o.optString("access_key")
    var isProcessing = o.optInt("processing", 0) == 1
    var isLive = o.optInt("live", 0) == 1
    var isUpcoming = o.optInt("upcoming", 0) == 1
    var isFavorite = o.optBoolean("favorite")

}