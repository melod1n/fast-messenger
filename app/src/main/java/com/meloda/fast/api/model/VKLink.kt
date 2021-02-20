package com.meloda.fast.api.model

import org.json.JSONObject
import java.io.Serializable

class VKLink(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var url: String = o.optString("url")
    var title: String = o.optString("title")
    var caption: String = o.optString("caption")
    var description: String = o.optString("description")
    var previewPage: String = o.optString("preview_page")
    var previewUrl: String = o.optString("preview_url")
    var photo: VKPhoto? = null
    var button: Button? = null

    init {
        o.optJSONObject("photo")?.let {
            photo = VKPhoto(it)
        }

        o.optJSONObject("button")?.let {
            button = Button(it)
        }
    }

    class Button(o: JSONObject) : Serializable {
        var title: String = o.optString("title")
        var action: Action? = null

        init {
            o.optJSONObject("action")?.let {
                action = Action(it)
            }
        }

        class Action(o: JSONObject) : Serializable {

            var type: String = o.optString("type")
            var url: String = o.optString("url")

        }
    }
}