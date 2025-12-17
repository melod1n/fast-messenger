package dev.meloda.fast.model.api.domain

import androidx.compose.runtime.Immutable
import dev.meloda.fast.model.database.VkMessageEntity

@Immutable
data class VkMessage(
    val id: Long,
    val cmId: Long,
    val text: String?,
    val isOut: Boolean,
    val peerId: Long,
    val fromId: Long,
    val date: Int,
    val randomId: Long,
    val action: Action?,
    val actionMemberId: Long?,
    val actionText: String?,
    val actionCmId: Long?,
    val actionMessage: String?,

    val updateTime: Int?,
    val pinnedAt: Int?,
    val isPinned: Boolean,
    val isImportant: Boolean,
    val isSpam: Boolean,

    val forwards: List<VkMessage>?,
    val attachments: List<VkAttachment>?,
    val replyMessage: VkMessage?,

    val formatData: FormatData?,

    val geoType: String?,
    val user: VkUser?,
    val group: VkGroupDomain?,
    val actionUser: VkUser?,
    val actionGroup: VkGroupDomain?,
) {

    fun isPeerChat() = peerId > 2_000_000_000

    fun isUser() = fromId > 0

    fun isGroup() = fromId < 0

    fun isRead(convo: VkConvo): Boolean = when {
        id <= 0 -> false
        else -> convo.isRead(this)
    }

    fun hasAttachments(): Boolean = attachments.orEmpty().isNotEmpty()

    fun hasReply(): Boolean = replyMessage != null

    fun hasForwards(): Boolean = !forwards.isNullOrEmpty()

    fun hasGeo(): Boolean = geoType != null

    fun isUpdated(): Boolean = updateTime != null && updateTime > 0

    fun isFailed(): Boolean = id <= -500_000

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

    data class FormatData(
        val version: String,
        val items: List<Item>
    ) {

        data class Item(
            val offset: Int,
            val length: Int,
            val type: FormatDataType,
            val url: String?
        )
    }
}

fun VkMessage.asEntity(): VkMessageEntity = VkMessageEntity(
    id = id,
    cmId = cmId,
    text = text,
    isOut = isOut,
    peerId = peerId,
    fromId = fromId,
    date = date,
    randomId = randomId,
    action = action?.value,
    actionMemberId = actionMemberId,
    actionText = actionText,
    actionCmId = actionCmId,
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
