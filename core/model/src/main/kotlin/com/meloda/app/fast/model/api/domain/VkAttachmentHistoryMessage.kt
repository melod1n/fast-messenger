package dev.meloda.fast.model.api.domain

data class VkAttachmentHistoryMessage(
    val messageId: Int,
    val conversationMessageId: Int,
    val date: Int,
    val fromId: Int,
    val position: Int,
    val attachment: VkAttachment
)
