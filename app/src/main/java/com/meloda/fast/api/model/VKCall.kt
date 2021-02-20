package com.meloda.fast.api.model

import org.json.JSONObject

class VKCall(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var initiatorId = o.optInt("initiator_id", -1)
    var receiverId = o.optInt("receiver_id", -1)
    var state: String = o.optString("state") //reached, canceled_by_initiator, canceled_by_receiver
    var time = o.optInt("time")
    var duration = o.optInt("duration")

}