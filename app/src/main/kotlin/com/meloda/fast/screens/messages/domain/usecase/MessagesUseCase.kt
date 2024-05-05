package com.meloda.fast.screens.messages.domain.usecase

import com.meloda.fast.api.model.domain.VkAttachment
import com.meloda.fast.api.model.domain.VkMessageDomain
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
    ): Flow<State<List<VkMessageDomain>>>

    fun getById(
        messageId: Int,
        extended: Boolean?,
        fields: String?
    ): Flow<State<VkMessageDomain?>>

    fun getByIds(
        messageIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessageDomain>>>

    fun sendMessage(
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): Flow<State<Int>>

    suspend fun storeMessage(message: VkMessageDomain)
    suspend fun storeMessages(messages: List<VkMessageDomain>)
}
