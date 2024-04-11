package com.meloda.fast.api.model.domain

import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

interface VkAttachment {
    val type: AttachmentType
        get() = AttachmentType.UNKNOWN

    fun asString(withAccessKey: Boolean = true): String = ""

    fun getUiText(): UiText = UiText.Empty
}

interface VkMultipleAttachment : VkAttachment {

    override fun getUiText(): UiText = getUiText(0)

    fun getUiText(size: Int): UiText = UiText.Empty
}
