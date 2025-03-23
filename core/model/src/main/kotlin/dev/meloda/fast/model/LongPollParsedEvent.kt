package dev.meloda.fast.model

import dev.meloda.fast.model.api.domain.VkMessage

sealed interface LongPollParsedEvent {

    data class NewMessage(val message: VkMessage) : LongPollParsedEvent

    data class MessageEdited(val message: VkMessage) : LongPollParsedEvent

    data class IncomingMessageRead(
        val peerId: Int,
        val messageId: Int,
        val unreadCount: Int,
    ) : LongPollParsedEvent

    data class OutgoingMessageRead(
        val peerId: Int,
        val messageId: Int,
        val unreadCount: Int,
    ) : LongPollParsedEvent

    data class ChatMajorChanged(
        val peerId: Int,
        val majorId: Int,
    ) : LongPollParsedEvent

    data class ChatMinorChanged(
        val peerId: Int,
        val minorId: Int
    ) : LongPollParsedEvent

    data class Interaction(
        val interactionType: InteractionType,
        val peerId: Int,
        val userIds: List<Int>,
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
        val peerId: Int,
        val messageId: Int,
        val marked: Boolean
    ) : LongPollParsedEvent

    data class MessageMarkedAsSpam(
        val peerId: Int,
        val messageId: Int
    ) : LongPollParsedEvent

    data class MessageMarkedAsNotSpam(
        val message: VkMessage
    ) : LongPollParsedEvent

    data class MessageDeleted(
        val peerId: Int,
        val messageId: Int,
        val forAll: Boolean
    ) : LongPollParsedEvent

    data class MessageRestored(
        val message: VkMessage
    ) : LongPollParsedEvent

    data class AudioMessageListened(
        val peerId: Int,
        val messageId: Int
    ) : LongPollParsedEvent

    data class ChatCleared(
        val peerId: Int,
        val toMessageId: Int
    ): LongPollParsedEvent
}
