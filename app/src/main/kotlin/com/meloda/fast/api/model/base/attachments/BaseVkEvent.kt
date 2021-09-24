package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkEvent
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkEvent(
    val button_text: String,
    val id: Int,
    val is_favorite: Boolean,
    val text: String,
    val address: String,
    val friends: List<Int> = listOf(),
    val member_status: Int,
    val time: Int
) : BaseVkAttachment() {

    fun asVkEvent() = VkEvent(
        id = id
    )

}