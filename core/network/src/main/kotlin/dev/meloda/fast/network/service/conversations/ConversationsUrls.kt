package dev.meloda.fast.network.service.conversations

import dev.meloda.fast.common.AppConstants

object ConversationsUrls {

    private const val URL = AppConstants.URL_API

    const val GET = "$URL/messages.getConversations"
    const val GET_BY_ID = "$URL/messages.getConversationsById"
    const val DELETE = "$URL/messages.deleteConversation"
    const val PIN = "$URL/messages.pinConversation"
    const val UNPIN = "$URL/messages.unpinConversation"
    const val REORDER_PINNED = "$URL/messages.reorderPinnedConversations"
    const val ARCHIVE = "$URL/messages.archiveConversation"
    const val UNARCHIVE = "$URL/messages.unarchiveConversation"
}
