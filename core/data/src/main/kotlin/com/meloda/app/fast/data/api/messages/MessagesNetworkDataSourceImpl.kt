package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.model.api.data.VkMessageData
import com.meloda.app.fast.model.api.data.asDomain
import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.model.api.requests.MessagesGetHistoryRequest
import com.meloda.app.fast.network.mapResult
import com.meloda.app.fast.network.service.messages.MessagesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesNetworkDataSourceImpl(
    private val messagesService: MessagesService
) : MessagesNetworkDataSource {
    override suspend fun getMessagesHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?
    ): List<VkMessage> = withContext(Dispatchers.IO) {
        messagesService.getHistory(
            MessagesGetHistoryRequest(
                count = count,
                offset = offset,
                peerId = conversationId,
                extended = true,
                startMessageId = null,
                rev = null,
                fields = VkConstants.ALL_FIELDS
            ).map
        ).mapResult(
            successMapper = { r ->
                val response = r.requireResponse()
                response.items.map(VkMessageData::asDomain)
            },
            errorMapper = { error -> error?.toDomain() }
        )

        // TODO: 05/05/2024, Danil Nikolaev: get messages

        emptyList()
    }

    override suspend fun getMessage(messageId: Int): VkMessage? = withContext(Dispatchers.IO) {
        // TODO: 05/05/2024, Danil Nikolaev: get message

        null
    }
}
