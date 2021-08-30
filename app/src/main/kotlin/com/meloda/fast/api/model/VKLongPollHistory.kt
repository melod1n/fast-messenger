package com.meloda.fast.api.model

import java.util.*

class VKLongPollHistory : VKModel() {

    override val attachmentType = VKAttachments.Type.NONE

    private val lpMessages: ArrayList<VKMessage>? = null
    private val messages: ArrayList<VKMessage>? = null
    private val profiles: ArrayList<VKUser>? = null
    private val groups: ArrayList<VKGroup>? = null //TODO: использовать

}