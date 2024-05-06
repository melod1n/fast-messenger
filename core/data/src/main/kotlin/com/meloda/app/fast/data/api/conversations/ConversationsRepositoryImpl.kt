package com.meloda.app.fast.data.api.conversations

import com.meloda.app.fast.database.dao.ConversationDao
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.asEntity
import com.meloda.app.fast.network.service.conversations.ConversationsService

// TODO: 05/05/2024, Danil Nikolaev: implement
class ConversationsRepositoryImpl(
    private val conversationsService: ConversationsService,
    private val conversationDao: ConversationDao
) : ConversationsRepository {
    override suspend fun getConversations(count: Int?, offset: Int?): List<VkConversation> {
        return emptyList()
    }

    override suspend fun storeConversations(conversations: List<VkConversation>) {
        conversationDao.insertAll(conversations.map(VkConversation::asEntity))
    }

    //    override suspend fun getConversations(
//        params: ConversationsGetRequest
//    ): ApiResult<ConversationsGetResponse, RestApiErrorDomain> =
//        withContext(Dispatchers.IO) {
//            conversationsService.getConversations(params.map).mapResult(
//                successMapper = { response -> response.requireResponse() },
//                errorMapper = { error -> error?.toDomain() }
//            )
//        }
//
//
//    override suspend fun delete(
//        params: ConversationsDeleteRequest
//    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        conversationsService.delete(params.map).mapResult(
//            successMapper = {},
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun pin(
//        params: ConversationsPinRequest
//    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        conversationsService.pin(params.map).mapResult(
//            successMapper = {},
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun unpin(
//        params: ConversationsUnpinRequest
//    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        conversationsService.unpin(params.map).mapResult(
//            successMapper = {},
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun reorderPinned(
//        params: ConversationsReorderPinnedRequest
//    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        conversationsService.reorderPinned(params.map).mapResult(
//            successMapper = {},
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
}
