package com.meloda.fast.api.model.old

import java.util.*

class VKLongPollHistory : VKModel() {

    override val attachmentType = VKAttachments.Type.NONE

    private val lpMessages: ArrayList<oldVKMessage>? = null
    private val messages: ArrayList<oldVKMessage>? = null
    private val profiles: ArrayList<oldVKUser>? = null
    private val groups: ArrayList<oldVKGroup>? = null //TODO: использовать

}