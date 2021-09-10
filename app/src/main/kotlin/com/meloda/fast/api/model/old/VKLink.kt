package com.meloda.fast.api.model.old

import org.json.JSONObject
import java.io.Serializable

class VKLink() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.LINK

    var url: String = ""
    var title: String = ""
    var caption: String = ""
    var description: String = ""
    var previewPage: String = ""
    var previewUrl: String = ""
    var photo: VKPhoto? = null
    var button: Button? = null

    constructor(o: JSONObject): this() {
        url = o.optString("url")
        title = o.optString("title")
        caption = o.optString("caption")
        description = o.optString("description")
        previewPage = o.optString("preview_page")
        previewUrl = o.optString("preview_url")

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