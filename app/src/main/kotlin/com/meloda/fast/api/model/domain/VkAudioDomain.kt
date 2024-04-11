package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkAudioDomain(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val artist: String,
    val url: String,
    val duration: Int,
    val accessKey: String?
) : VkMultipleAttachment {

    override val type: AttachmentType = AttachmentType.AUDIO

    val className: String = this::class.java.name

    override fun getUiText(size: Int): UiText =
        UiText.QuantityResource(R.plurals.attachment_audios, size)

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )
}
