package com.meloda.fast.api.network.conversations

import com.meloda.fast.api.network.VkUrls

object ConversationsUrls {

    const val Get = "${VkUrls.API}/messages.getConversations"
    const val Delete = "${VkUrls.API}/messages.deleteConversation"
    const val Pin = "${VkUrls.API}/messages.pinConversation"
    const val Unpin = "${VkUrls.API}/messages.unpinConversation"
    const val ReorderPinned = "${VkUrls.API}/messages.reorderPinnedConversations"

}