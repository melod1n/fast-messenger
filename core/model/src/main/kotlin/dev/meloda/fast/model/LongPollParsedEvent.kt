package dev.meloda.fast.model

import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkMessage

sealed interface LongPollParsedEvent {

    data class NewMessage(
        val message: VkMessage,
        val inArchive: Boolean
    ) : LongPollParsedEvent

    data class MessageEdited(val message: VkMessage) : LongPollParsedEvent

    data class MessageUpdated(val message: VkMessage) : LongPollParsedEvent

    data class MessageCacheClear(val message: VkMessage) : LongPollParsedEvent

    data class IncomingMessageRead(
        val peerId: Long,
        val cmId: Long,
        val unreadCount: Int,
    ) : LongPollParsedEvent

    data class OutgoingMessageRead(
        val peerId: Long,
        val cmId: Long,
        val unreadCount: Int,
    ) : LongPollParsedEvent

    data class ChatMajorChanged(
        val peerId: Long,
        val majorId: Int,
    ) : LongPollParsedEvent

    data class ChatMinorChanged(
        val peerId: Long,
        val minorId: Int
    ) : LongPollParsedEvent

    data class Interaction(
        val interactionType: InteractionType,
        val peerId: Long,
        val userIds: List<Long>,
        val totalCount: Int,
        val timestamp: Int
    ) : LongPollParsedEvent

    data class UnreadCounter(
        val unread: Int,
        val unreadUnmuted: Int,
        val showOnlyMuted: Boolean,
        val business: Int,
        val archive: Int,
        val archiveUnmuted: Int,
        val archiveMentions: Int
    ) : LongPollParsedEvent

    data class MessageMarkedAsImportant(
        val peerId: Long,
        val cmId: Long,
        val marked: Boolean
    ) : LongPollParsedEvent

    data class MessageMarkedAsSpam(
        val peerId: Long,
        val cmId: Long
    ) : LongPollParsedEvent

    data class MessageMarkedAsNotSpam(
        val message: VkMessage
    ) : LongPollParsedEvent

    data class MessageDeleted(
        val peerId: Long,
        val cmId: Long,
        val forAll: Boolean
    ) : LongPollParsedEvent

    data class MessageRestored(
        val message: VkMessage
    ) : LongPollParsedEvent

    data class AudioMessageListened(
        val peerId: Long,
        val cmId: Long
    ) : LongPollParsedEvent

    data class ChatCleared(
        val peerId: Long,
        val toCmId: Long
    ) : LongPollParsedEvent

    data class ChatArchived(
        val conversation: VkConversation,
        val archived: Boolean
    ) : LongPollParsedEvent
}
