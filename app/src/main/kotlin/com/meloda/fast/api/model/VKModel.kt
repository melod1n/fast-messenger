package com.meloda.fast.api.model

import com.meloda.fast.base.adapter.BaseItem
import java.io.Serializable

abstract class VKModel : BaseItem(), Serializable {

    abstract val attachmentType: VKAttachments.Type

    companion object {
        const val serialVersionUID = 1L
    }

}