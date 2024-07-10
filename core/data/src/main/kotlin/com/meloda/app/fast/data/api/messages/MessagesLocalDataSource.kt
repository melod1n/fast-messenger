package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.model.database.VkMessageEntity

interface MessagesLocalDataSource {

    suspend fun getMessages(
        conversationId: Int,
        offset: Int?,
        count: Int?
    ): List<VkMessageEntity>

    suspend fun getMessage(messageId: Int): VkMessageEntity?

    suspend fun storeMessages(messages: List<VkMessageEntity>)
}
