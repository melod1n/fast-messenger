package dev.meloda.fast.data.api.conversations

import com.slack.eithernet.ApiResult
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.VkGroupsMap
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.VkUsersMap
import dev.meloda.fast.database.dao.ConversationDao
import dev.meloda.fast.database.dao.GroupDao
import dev.meloda.fast.database.dao.MessageDao
import dev.meloda.fast.database.dao.UserDao
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkGroupData
import dev.meloda.fast.model.api.data.VkUserData
import dev.meloda.fast.model.api.data.asDomain
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkGroupDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.model.api.domain.asEntity
import dev.meloda.fast.model.api.requests.ConversationsDeleteRequest
import dev.meloda.fast.model.api.requests.ConversationsGetRequest
import dev.meloda.fast.model.api.requests.ConversationsPinRequest
import dev.meloda.fast.model.api.requests.ConversationsUnpinRequest
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.mapApiResult
import dev.meloda.fast.network.service.conversations.ConversationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationsRepositoryImpl(
    private val conversationsService: ConversationsService,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val groupDao: GroupDao,
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

                val conversations = response.items.map { item ->
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

                val messages = conversations.mapNotNull(VkConversation::lastMessage)

                launch(Dispatchers.IO) {
                    conversationDao.insertAll(conversations.map(VkConversation::asEntity))
                    messageDao.insertAll(messages.map(VkMessage::asEntity))
                    userDao.insertAll(profilesList.map(VkUser::asEntity))
                    groupDao.insertAll(groupsList.map(VkGroupDomain::asEntity))
                }

                conversations
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun getConversationsById(
        peerIds: List<Long>
    ): ApiResult<List<VkConversation>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestParams = mapOf(
            "peer_ids" to peerIds.joinToString(separator = ","),
            "extended" to "1",
            "fields" to VkConstants.ALL_FIELDS
        )

        conversationsService.getConversationsById(requestParams).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()

                val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
                val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)
                val contactsList = response.contacts.orEmpty().map(VkContactData::mapToDomain)

                val usersMap = VkUsersMap.forUsers(profilesList)
                val groupsMap = VkGroupsMap.forGroups(groupsList)

                val conversations = response.items.map { item ->
                    item.asDomain().let { conversation ->
                        conversation.copy(
                            user = usersMap.conversationUser(conversation),
                            group = groupsMap.conversationGroup(conversation)
                        ).also { VkMemoryCache[conversation.id] = it }
                    }
                }

                launch(Dispatchers.IO) {
                    conversationDao.insertAll(conversations.map(VkConversation::asEntity))
                    userDao.insertAll(profilesList.map(VkUser::asEntity))
                    groupDao.insertAll(groupsList.map(VkGroupDomain::asEntity))
                }

                VkMemoryCache.appendUsers(profilesList)
                VkMemoryCache.appendGroups(groupsList)
                VkMemoryCache.appendContacts(contactsList)

                conversations
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun storeConversations(conversations: List<VkConversation>) {
        conversationDao.insertAll(conversations.map(VkConversation::asEntity))
    }

    override suspend fun delete(peerId: Long): ApiResult<Long, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            val requestModel = ConversationsDeleteRequest(peerId = peerId)

            conversationsService.delete(requestModel.map).mapApiResult(
                successMapper = { response -> response.requireResponse().lastDeletedId },
                errorMapper = { error -> error?.toDomain() }
            )
        }

    override suspend fun pin(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ConversationsPinRequest(peerId = peerId)
        conversationsService.pin(requestModel.map).mapApiDefault()
    }

    override suspend fun unpin(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ConversationsUnpinRequest(peerId = peerId)
        conversationsService.unpin(requestModel.map).mapApiDefault()
    }
}
