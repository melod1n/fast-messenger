package com.meloda.app.fast.network.service.conversations

import com.meloda.app.fast.common.AppConstants

object ConversationsUrls {

    const val GET = "${AppConstants.URL_API}/messages.getConversations"
    const val DELETE = "${AppConstants.URL_API}/messages.deleteConversation"
    const val PIN = "${AppConstants.URL_API}/messages.pinConversation"
    const val UNPIN = "${AppConstants.URL_API}/messages.unpinConversation"
    const val REORDER_PINNED = "${AppConstants.URL_API}/messages.reorderPinnedConversations"
}
