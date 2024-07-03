package com.meloda.app.fast.data.api.conversations

import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.data.VkGroupsMap
import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.data.VkUsersMap
import com.meloda.app.fast.database.dao.ConversationDao
import com.meloda.app.fast.model.api.data.VkContactData
import com.meloda.app.fast.model.api.data.VkGroupData
import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.model.api.data.asDomain
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.asEntity
import com.meloda.app.fast.model.api.requests.ConversationsDeleteRequest
import com.meloda.app.fast.model.api.requests.ConversationsGetRequest
import com.meloda.app.fast.model.api.requests.ConversationsPinRequest
import com.meloda.app.fast.model.api.requests.ConversationsUnpinRequest
import com.meloda.app.fast.network.RestApiErrorDomain
import com.meloda.app.fast.network.mapApiResult
import com.meloda.app.fast.network.mapResult
import com.meloda.app.fast.network.service.conversations.ConversationsService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConversationsRepositoryImpl(
    private val conversationsService: ConversationsService,
    private val conversationDao: ConversationDao
) : ConversationsRepository {

    override suspend fun getConversations(
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkConversation>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ConversationsGetRequest(
            count = count,
            offset = offset,
            fields = VkConstants.ALL_FIELDS,
            filter = "all",
            extended = true,
            startMessageId = null
        )

        conversationsService.getConversations(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()

                val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
                val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)
                val contactsList = response.contacts.orEmpty().map(VkContactData::mapToDomain)

                val usersMap = VkUsersMap.forUsers(profilesList)
                val groupsMap = VkGroupsMap.forGroups(groupsList)

                VkMemoryCache.appendUsers(profilesList)
                VkMemoryCache.appendGroups(groupsList)
                VkMemoryCache.appendContacts(contactsList)

                response.items.map { item ->
                    val lastMessage = item.lastMessage?.asDomain()?.let { message ->
                        message.copy(
                            user = usersMap.messageUser(message),
                            group = groupsMap.messageGroup(message),
                            actionUser = usersMap.messageActionUser(message),
                            actionGroup = groupsMap.messageActionGroup(message)
                        ).also { VkMemoryCache[message.id] = it }
                    }
                    item.conversation.asDomain(lastMessage).let { conversation ->
                        conversation.copy(
                            user = usersMap.conversationUser(conversation),
                            group = groupsMap.conversationGroup(conversation)
                        ).also { VkMemoryCache[conversation.id] = it }
                    }
                }
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun storeConversations(conversations: List<VkConversation>) {
        conversationDao.insertAll(conversations.map(VkConversation::asEntity))
    }

    override suspend fun delete(
        peerId: Int
    ): ApiResult<Boolean, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ConversationsDeleteRequest(peerId = peerId)

        conversationsService.delete(requestModel.map).mapResult(
            successMapper = { true },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun pin(
        peerId: Int
    ): ApiResult<Boolean, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ConversationsPinRequest(peerId = peerId)

        conversationsService.pin(requestModel.map).mapResult(
            successMapper = { true },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun unpin(
        peerId: Int
    ): ApiResult<Boolean, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ConversationsUnpinRequest(peerId = peerId)

        conversationsService.unpin(requestModel.map).mapResult(
            successMapper = { true },
            errorMapper = { error -> error?.toDomain() }
        )
    }
}
