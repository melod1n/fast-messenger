package com.meloda.fast.api.model.domain

import com.meloda.fast.api.VkUtils

data class VkAudioDomain(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val artist: String,
    val url: String,
    val duration: Int,
    val accessKey: String?
) : VkAttachment {

    val className: String = this::class.java.name

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )
}
