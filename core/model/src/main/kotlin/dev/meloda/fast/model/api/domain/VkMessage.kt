package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.database.VkMessageEntity

data class VkMessage(
    val id: Int,
    val conversationMessageId: Int,
    val text: String?,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int,
    val randomId: Int,
    val action: Action?,
    val actionMemberId: Int?,
    val actionText: String?,
    val actionConversationMessageId: Int?,
    val actionMessage: String?,

    val updateTime: Int?,
    val pinnedAt: Int?,
    val isPinned: Boolean,
    val isImportant: Boolean = false,

    val forwards: List<VkMessage>?,
    val attachments: List<VkAttachment>?,
    val replyMessage: VkMessage?,

    val geoType: String?,
    val user: VkUser?,
    val group: VkGroupDomain?,
    val actionUser: VkUser?,
    val actionGroup: VkGroupDomain?
) {

    fun isPeerChat() = peerId > 2_000_000_000

    fun isUser() = fromId > 0

    fun isGroup() = fromId < 0

    fun isRead(conversation: VkConversation): Boolean = when {
        id <= 0 -> false
        isOut -> conversation.outRead - id >= 0
        else -> conversation.inRead - id >= 0
    }

    fun hasAttachments(): Boolean = attachments.orEmpty().isNotEmpty()

    fun hasReply(): Boolean = replyMessage != null

    fun hasForwards(): Boolean = !forwards.isNullOrEmpty()

    fun hasGeo(): Boolean = geoType != null

    fun isUpdated(): Boolean = updateTime != null && updateTime > 0

    enum class Action(val value: String) {
        CHAT_CREATE("chat_create"),
        CHAT_PHOTO_UPDATE("chat_photo_update"),
        CHAT_PHOTO_REMOVE("chat_photo_remove"),
        CHAT_TITLE_UPDATE("chat_title_update"),
        CHAT_PIN_MESSAGE("chat_pin_message"),
        CHAT_UNPIN_MESSAGE("chat_unpin_message"),
        CHAT_INVITE_USER("chat_invite_user"),
        CHAT_INVITE_USER_BY_LINK("chat_invite_user_by_link"),
        CHAT_KICK_USER("chat_kick_user"),
        CHAT_SCREENSHOT("chat_screenshot"),

        CHAT_INVITE_USER_BY_CALL("chat_invite_user_by_call"),
        CHAT_INVITE_USER_BY_CALL_LINK("chat_invite_user_by_call_join_link"),
        CHAT_STYLE_UPDATE("conversation_style_update");

        companion object {
            fun parse(value: String?): Action? = entries.firstOrNull { it.value == value }
        }
    }
}

fun VkMessage.asEntity(): VkMessageEntity = VkMessageEntity(
    id = id,
    conversationMessageId = conversationMessageId,
    text = text,
    isOut = isOut,
    peerId = peerId,
    fromId = fromId,
    date = date,
    randomId = randomId,
    action = action?.value,
    actionMemberId = actionMemberId,
    actionText = actionText,
    actionConversationMessageId = actionConversationMessageId,
    actionMessage = actionMessage,
    updateTime = updateTime,
    important = isImportant,
    forwardIds = forwards.orEmpty().map(VkMessage::id),
    // TODO: 05/05/2024, Danil Nikolaev: save attachments
    attachments = emptyList(),
    replyMessageId = replyMessage?.id,
    geoType = geoType,
    pinnedAt = pinnedAt,
    isPinned = isPinned,
)
