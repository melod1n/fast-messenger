package com.meloda.fast.api.network.messages

import com.meloda.fast.api.network.VkUrls

object MessagesUrls {

    const val GetHistory = "${VkUrls.API}/messages.getHistory"
    const val Send = "${VkUrls.API}/messages.send"
    const val MarkAsImportant = "${VkUrls.API}/messages.markAsImportant"
    const val GetLongPollServer = "${VkUrls.API}/messages.getLongPollServer"
    const val GetLongPollHistory = "${VkUrls.API}/messages.getLongPollHistory"
    const val Pin = "${VkUrls.API}/messages.pin"
    const val Unpin = "${VkUrls.API}/messages.unpin"
    const val Delete = "${VkUrls.API}/messages.delete"
    const val Edit = "${VkUrls.API}/messages.edit"
    const val GetById = "${VkUrls.API}/messages.getById"
    const val MarkAsRead = "${VkUrls.API}/messages.markAsRead"
    const val GetChat = "${VkUrls.API}/messages.getChat"
    const val GetConversationMembers = "${VkUrls.API}/messages.getConversationMembers"

}