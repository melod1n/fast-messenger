package dev.meloda.fast.messageshistory.util

import dev.meloda.fast.messageshistory.model.UiItem

fun List<UiItem>.firstMessage(): UiItem.Message = filterIsInstance<UiItem.Message>().first()

fun List<UiItem>.firstMessageOrNull(): UiItem.Message? = filterIsInstance<UiItem.Message>().firstOrNull()

fun List<UiItem>.indexOfMessageById(messageId: Int): Int =
    indexOfFirst { it.id == messageId }

fun List<UiItem>.findMessageById(messageId: Int): UiItem.Message? =
    firstOrNull { it.id == messageId } as UiItem.Message?

fun List<UiItem>.indexOfMessageByCmId(cmId: Int): Int =
    indexOfFirst { it.cmId == cmId }

fun List<UiItem>.findMessageByCmId(cmId: Int): UiItem.Message =
    first { it.cmId == cmId } as UiItem.Message
