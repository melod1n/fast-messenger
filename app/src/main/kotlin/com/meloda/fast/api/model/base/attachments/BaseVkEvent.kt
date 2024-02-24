package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkEvent
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
data class BaseVkEvent(
    val button_text: String,
    val id: Int,
    val is_favorite: Boolean,
    val text: String,
    val address: String,
    val friends: List<Int> = emptyList(),
    val member_status: Int,
    val time: Int
) : BaseVkAttachment() {

    fun asVkEvent() = VkEvent(id = id)

}
