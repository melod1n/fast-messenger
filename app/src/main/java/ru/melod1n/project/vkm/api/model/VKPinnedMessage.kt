package ru.melod1n.project.vkm.api.model

import org.json.JSONObject
import java.util.*

class VKPinnedMessage(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var id = o.optInt("id", -1)
    var date = o.optInt("date")
    var fromId = o.optInt("from_id", -1)
    var text: String = o.optString("text")
    var attachments: ArrayList<VKModel>? = null
    var fwdMessages: ArrayList<VKMessage>? = null

    init {
        o.optJSONArray("attachments")?.let {
            attachments = VKAttachments.parse(it)
        }

        //TODO: parse forwarded
    }

}