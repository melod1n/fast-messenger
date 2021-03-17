package com.meloda.vksdk.model

import org.json.JSONObject

class VKCall() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.CALL

    var initiatorId: Int = 0
    var receiverId: Int = 0
    var state: State = State.NONE
    var time: Int = 0
    var duration: Int = 0

    constructor(o: JSONObject) : this() {
        initiatorId = o.optInt("initiator_id", -1)
        receiverId = o.optInt("receiver_id", -1)
        state = State.fromString(o.optString("state"))
        time = o.optInt("time")
        duration = o.optInt("duration")
    }

    enum class State(val value: String) {
        NONE("none"),
        REACHED("reached"),
        CANCELLED_INITIATOR("canceled_by_initiator"),
        CANCELLED_RECEIVER("canceled_by_receiver");

        companion object {
            fun fromString(value: String) = values().first { it.value == value }
        }
    }

}