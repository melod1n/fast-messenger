package dev.meloda.fast.network.service.messages

import dev.meloda.fast.common.AppConstants

object MessagesUrls {
    
    private const val URL = AppConstants.URL_API
    
    const val GET_HISTORY = "$URL/messages.getHistory"
    const val SEND = "$URL/messages.send"
    const val MARK_AS_IMPORTANT = "$URL/messages.markAsImportant"
    const val GET_LONG_POLL_SERVER = "$URL/messages.getLongPollServer"
    const val GET_LONG_POLL_HISTORY = "$URL/messages.getLongPollHistory"
    const val PIN = "$URL/messages.pin"
    const val UNPIN = "$URL/messages.unpin"
    const val DELETE = "$URL/messages.delete"
    const val EDIT = "$URL/messages.edit"
    const val GET_BY_ID = "$URL/messages.getById"
    const val MARK_AS_READ = "$URL/messages.markAsRead"
    const val GET_CHAT = "$URL/messages.getChat"
    const val GET_CONVOS_MEMBERS = "$URL/messages.getConversationMembers"
    const val REMOVE_CHAT_USER = "$URL/messages.removeChatUser"
    const val GET_HISTORY_ATTACHMENTS = "$URL/messages.getHistoryAttachments"
    const val CREATE_CHAT = "$URL/messages.createChat"
    const val GET_MESSAGE_READ_PEERS = "$URL/messages.getMessageReadPeers"
}
