package dev.meloda.fast.network.service.conversations

import dev.meloda.fast.common.AppConstants

object ConversationsUrls {

    const val GET = "${AppConstants.URL_API}/messages.getConversations"
    const val DELETE = "${AppConstants.URL_API}/messages.deleteConversation"
    const val PIN = "${AppConstants.URL_API}/messages.pinConversation"
    const val UNPIN = "${AppConstants.URL_API}/messages.unpinConversation"
    const val REORDER_PINNED = "${AppConstants.URL_API}/messages.reorderPinnedConversations"
}
