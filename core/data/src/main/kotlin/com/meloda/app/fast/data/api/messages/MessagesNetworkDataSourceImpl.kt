package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.data.VkGroupsMap
import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.data.VkUsersMap
import com.meloda.app.fast.model.api.data.VkContactData
import com.meloda.app.fast.model.api.data.VkGroupData
import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.model.api.data.asDomain
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.model.api.requests.MessagesGetHistoryRequest
import com.meloda.app.fast.network.RestApiErrorDomain
import com.meloda.app.fast.network.mapApiResult
import com.meloda.app.fast.network.service.messages.MessagesService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesNetworkDataSourceImpl(
    private val messagesService: MessagesService
) : MessagesNetworkDataSource {

    override suspend fun getMessagesHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?
    ): ApiResult<MessagesHistoryDomain, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesGetHistoryRequest(
            count = count,
            offset = offset,
            peerId = conversationId,
            extended = true,
            startMessageId = null,
            rev = null,
            fields = VkConstants.ALL_FIELDS
        )

        messagesService.getHistory(requestModel.map).mapApiResult(
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

                val messages = response.items.map { item ->
                    item.asDomain().let { message ->
                        message.copy(
                            user = usersMap.messageUser(message),
                            group = groupsMap.messageGroup(message),
                            actionUser = usersMap.messageActionUser(message),
                            actionGroup = groupsMap.messageActionGroup(message)
                        ).also { VkMemoryCache[message.id] = it }
                    }
                }

                val conversations = response.conversations.orEmpty().map { item ->
                    val message = messages.firstOrNull { it.id == item.lastMessageId }
                    item.asDomain(message)
                        .let { conversation ->
                            conversation.copy(
                                user = usersMap.conversationUser(conversation),
                                group = groupsMap.conversationGroup(conversation)
                            ).also { VkMemoryCache[conversation.id] = it }
                        }
                }

                MessagesHistoryDomain(
                    messages = messages,
                    conversations = conversations
                )
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun getMessage(messageId: Int): VkMessage? = withContext(Dispatchers.IO) {
        // TODO: 05/05/2024, Danil Nikolaev: get message
        null
    }
}

data class MessagesHistoryDomain(
    val messages: List<VkMessage>,
    val conversations: List<VkConversation>
)
