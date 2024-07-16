package com.meloda.app.fast.messageshistory.util

import com.meloda.app.fast.messageshistory.model.UiItem

fun List<UiItem>.firstMessage(): UiItem.Message = first() as UiItem.Message

fun List<UiItem>.indexOfMessageById(messageId: Int): Int =
    indexOfFirst { it.id == messageId }

fun List<UiItem>.findMessageById(messageId: Int): UiItem.Message =
    first { it.id == messageId } as UiItem.Message

fun List<UiItem>.indexOfMessageByCmId(cmId: Int): Int =
    indexOfFirst { it.cmId == cmId }

fun List<UiItem>.findMessageByCmId(cmId: Int): UiItem.Message =
    first { it.cmId == cmId } as UiItem.Message
