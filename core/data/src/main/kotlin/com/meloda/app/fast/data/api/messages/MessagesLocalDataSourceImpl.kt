package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.database.dao.MessageDao
import com.meloda.app.fast.model.database.VkMessageEntity

// TODO: 05/05/2024, Danil Nikolaev: use paging for room
class MessagesLocalDataSourceImpl(
    private val messageDao: MessageDao
) : MessagesLocalDataSource {

    override suspend fun getMessages(
        conversationId: Int,
        offset: Int?,
        count: Int?
    ): List<VkMessageEntity> = messageDao.getAll(conversationId)

    override suspend fun getMessage(
        messageId: Int
    ): VkMessageEntity? = messageDao.getById(messageId)

    override suspend fun storeMessages(messages: List<VkMessageEntity>) {
        messageDao.insertAll(messages)
    }
}
