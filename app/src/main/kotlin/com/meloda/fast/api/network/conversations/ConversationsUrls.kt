package com.meloda.fast.api.network.conversations

import com.meloda.fast.api.network.VkUrls

object ConversationsUrls {

    const val GET = "${VkUrls.API}/messages.getConversations"
    const val DELETE = "${VkUrls.API}/messages.deleteConversation"
    const val PIN = "${VkUrls.API}/messages.pinConversation"
    const val UNPIN = "${VkUrls.API}/messages.unpinConversation"
    const val REORDER_PINNED = "${VkUrls.API}/messages.reorderPinnedConversations"
}
