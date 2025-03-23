package dev.meloda.fast.network.service.messages

import dev.meloda.fast.common.AppConstants

object MessagesUrls {

    const val GET_HISTORY = "${AppConstants.URL_API}/messages.getHistory"
    const val SEND = "${AppConstants.URL_API}/messages.send"
    const val MARK_AS_IMPORTANT = "${AppConstants.URL_API}/messages.markAsImportant"
    const val GET_LONG_POLL_SERVER = "${AppConstants.URL_API}/messages.getLongPollServer"
    const val GET_LONG_POLL_HISTORY = "${AppConstants.URL_API}/messages.getLongPollHistory"
    const val PIN = "${AppConstants.URL_API}/messages.pin"
    const val UNPIN = "${AppConstants.URL_API}/messages.unpin"
    const val DELETE = "${AppConstants.URL_API}/messages.delete"
    const val EDIT = "${AppConstants.URL_API}/messages.edit"
    const val GET_BY_ID = "${AppConstants.URL_API}/messages.getById"
    const val MARK_AS_READ = "${AppConstants.URL_API}/messages.markAsRead"
    const val GET_CHAT = "${AppConstants.URL_API}/messages.getChat"
    const val GET_CONVERSATIONS_MEMBERS = "${AppConstants.URL_API}/messages.getConversationMembers"
    const val REMOVE_CHAT_USER = "${AppConstants.URL_API}/messages.removeChatUser"
    const val GET_HISTORY_ATTACHMENTS = "${AppConstants.URL_API}/messages.getHistoryAttachments"
    const val CREATE_CHAT = "${AppConstants.URL_API}/messages.createChat"
}
