package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkStory(
    val id: Int,
    val ownerId: Int,
    val date: Int,
    val photo: VkPhoto?
) : VkAttachment() {

    fun isFromUser() = ownerId > 0

    fun isFromGroup() = ownerId < 0

    @IgnoredOnParcel
    val className: String = this::class.java.name

}
