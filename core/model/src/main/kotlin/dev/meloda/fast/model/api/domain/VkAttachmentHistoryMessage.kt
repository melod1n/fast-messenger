package dev.meloda.fast.model.api.domain

data class VkAttachmentHistoryMessage(
    val messageId: Long,
    val cmId: Long,
    val date: Int,
    val fromId: Long,
    val position: Int,
    val attachment: VkAttachment
)
