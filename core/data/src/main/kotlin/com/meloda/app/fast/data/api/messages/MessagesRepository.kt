package dev.meloda.fast.data.api.messages

import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface MessagesRepository {

    suspend fun getHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?
    ): ApiResult<MessagesHistoryInfo, RestApiErrorDomain>

    suspend fun getById(
        messagesIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): ApiResult<List<VkMessage>, RestApiErrorDomain>

    suspend fun send(
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun markAsRead(
        peerId: Int,
        startMessageId: Int?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getHistoryAttachments(
        peerId: Int,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Int
    ): ApiResult<List<VkAttachmentHistoryMessage>, RestApiErrorDomain>

    suspend fun storeMessages(messages: List<VkMessage>)

//    suspend fun markAsImportant(
//        params: MessagesMarkAsImportantRequest
//    ): ApiResult<List<Int>, RestApiErrorDomain>
//
//    suspend fun pin(
//        params: MessagesPinMessageRequest
//    ): ApiResult<VkMessageData, RestApiErrorDomain>
//
//    suspend fun unpin(
//        params: MessagesUnPinMessageRequest
//    ): ApiResult<Unit, RestApiErrorDomain>
//
//    suspend fun delete(
//        params: MessagesDeleteRequest
//    ): ApiResult<Unit, RestApiErrorDomain>
//
//    suspend fun edit(
//        params: MessagesEditRequest
//    ): ApiResult<Int, RestApiErrorDomain>
//
//    suspend fun getChat(
//        params: MessagesGetChatRequest
//    ): ApiResult<VkChatData, RestApiErrorDomain>
//
//    suspend fun getConversationMembers(
//        params: MessagesGetConversationMembersRequest
//    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain>
//
//    suspend fun removeChatUser(
//        params: MessagesRemoveChatUserRequest
//    ): ApiResult<Int, RestApiErrorDomain>
}
