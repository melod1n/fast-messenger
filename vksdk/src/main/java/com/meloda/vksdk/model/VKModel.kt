package com.meloda.vksdk.model

import java.io.Serializable

abstract class VKModel : Serializable {

    abstract val attachmentType: VKAttachments.Type

    companion object {
        const val serialVersionUID = 1L
    }

}