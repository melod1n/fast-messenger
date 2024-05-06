package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.model.api.domain.VkMessage

interface MessagesNetworkDataSource {

    suspend fun getMessagesHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?,
    ): List<VkMessage>

    suspend fun getMessage(messageId: Int): VkMessage?
}
