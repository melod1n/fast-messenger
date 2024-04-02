package com.meloda.fast.data.messages.domain.usecase

import com.meloda.fast.api.network.messages.MessagesGetByIdResponse
import com.meloda.fast.api.network.messages.MessagesGetHistoryResponse
import com.meloda.fast.base.State
import kotlinx.coroutines.flow.Flow

interface MessagesUseCase {

    fun getHistory(
        count: Int?,
        offset: Int?,
        peerId: Int,
        extended: Boolean?,
        startMessageId: Int?,
        rev: Boolean?,
        fields: String?,
    ): Flow<State<MessagesGetHistoryResponse>>

    fun getById(
        messagesIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<MessagesGetByIdResponse>>
}
