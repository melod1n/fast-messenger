package com.meloda.app.fast.data.api.conversations

import com.meloda.app.fast.model.api.domain.VkConversation

interface ConversationsRepository {

    suspend fun getConversations(
        count: Int?,
        offset: Int?
    ): List<VkConversation>

    suspend fun storeConversations(conversations: List<VkConversation>)

//    suspend fun delete(
//        params: ConversationsDeleteRequest
//    ): Boolean
//
//    suspend fun pin(
//        params: ConversationsPinRequest
//    ): Boolean
//
//    suspend fun unpin(
//        params: ConversationsUnpinRequest
//    ): Boolean
//
//    suspend fun reorderPinned(
//        params: ConversationsReorderPinnedRequest
//    ): Boolean
}
