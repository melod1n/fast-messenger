package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.model.api.domain.asEntity
import com.meloda.app.fast.model.database.VkMessageEntity
import com.meloda.app.fast.model.database.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// TODO: 05/05/2024, Danil Nikolaev: implement syncing
class MessagesRepositoryImpl(
    private val networkDataSource: MessagesNetworkDataSource,
    private val localDataSource: MessagesLocalDataSource
) : MessagesRepository {

    override suspend fun getMessages(
        conversationId: Int,
        offset: Int?,
        count: Int?
    ): Flow<List<VkMessage>> = flow {
        val localMessages = localDataSource.getMessages(
            conversationId = conversationId,
            offset = offset,
            count = count
        ).map(VkMessageEntity::asExternalModel)

        emit(localMessages)

        val networkMessages = networkDataSource.getMessagesHistory(
            conversationId = conversationId,
            offset = offset,
            count = count
        )

        emit(networkMessages)
    }

    override suspend fun getMessage(messageId: Int): Flow<VkMessage?> = flow {
        val localMessage = localDataSource.getMessage(messageId)?.asExternalModel()

        emit(localMessage)

        val networkMessage = networkDataSource.getMessage(messageId)

        emit(networkMessage)
    }

    override suspend fun storeMessages(messages: List<VkMessage>) {
        localDataSource.storeMessages(messages.map(VkMessage::asEntity))
    }

    //    override suspend fun getHistory(
//        params: MessagesGetHistoryRequest
//    ): ApiResult<MessagesGetHistoryResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.getHistory(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun send(
//        params: MessagesSendRequest
//    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.send(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun markAsImportant(
//        params: MessagesMarkAsImportantRequest
//    ): ApiResult<List<Int>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.markAsImportant(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun pin(
//        params: MessagesPinMessageRequest
//    ): ApiResult<VkMessageData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.pin(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun unpin(
//        params: MessagesUnPinMessageRequest
//    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.unpin(params.map).mapResult(
//            successMapper = {},
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun delete(
//        params: MessagesDeleteRequest
//    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.delete(params.map).mapResult(
//            successMapper = {},
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun edit(
//        params: MessagesEditRequest
//    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.edit(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun getById(
//        params: MessagesGetByIdRequest
//    ): ApiResult<MessagesGetByIdResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.getById(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun markAsRead(
//        params: MessagesMarkAsReadRequest
//    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.markAsRead(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun getChat(
//        params: MessagesGetChatRequest
//    ): ApiResult<VkChatData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.getChat(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun getConversationMembers(
//        params: MessagesGetConversationMembersRequest
//    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain> =
//        withContext(Dispatchers.IO) {
//            messagesService.getConversationMembers(params.map).mapResult(
//                successMapper = { response -> response.requireResponse() },
//                errorMapper = { error -> error?.toDomain() }
//            )
//        }
//
//    override suspend fun removeChatUser(
//        params: MessagesRemoveChatUserRequest
//    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.removeChatUser(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
}
