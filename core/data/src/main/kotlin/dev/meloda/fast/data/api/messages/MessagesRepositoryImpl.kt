package dev.meloda.fast.data.api.messages

import com.slack.eithernet.ApiResult
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.VkGroupsMap
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.VkUsersMap
import dev.meloda.fast.database.dao.ConversationDao
import dev.meloda.fast.database.dao.GroupDao
import dev.meloda.fast.database.dao.MessageDao
import dev.meloda.fast.database.dao.UserDao
import dev.meloda.fast.model.api.data.VkAttachmentHistoryMessageData
import dev.meloda.fast.model.api.data.VkChatData
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkGroupData
import dev.meloda.fast.model.api.data.VkUserData
import dev.meloda.fast.model.api.data.asDomain
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkGroupDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.model.api.domain.asEntity
import dev.meloda.fast.model.api.requests.MessagesCreateChatRequest
import dev.meloda.fast.model.api.requests.MessagesDeleteRequest
import dev.meloda.fast.model.api.requests.MessagesEditRequest
import dev.meloda.fast.model.api.requests.MessagesGetByIdRequest
import dev.meloda.fast.model.api.requests.MessagesGetChatRequest
import dev.meloda.fast.model.api.requests.MessagesGetConversationMembersRequest
import dev.meloda.fast.model.api.requests.MessagesGetHistoryAttachmentsRequest
import dev.meloda.fast.model.api.requests.MessagesGetHistoryRequest
import dev.meloda.fast.model.api.requests.MessagesMarkAsImportantRequest
import dev.meloda.fast.model.api.requests.MessagesMarkAsReadRequest
import dev.meloda.fast.model.api.requests.MessagesPinMessageRequest
import dev.meloda.fast.model.api.requests.MessagesRemoveChatUserRequest
import dev.meloda.fast.model.api.requests.MessagesSendRequest
import dev.meloda.fast.model.api.requests.MessagesUnpinMessageRequest
import dev.meloda.fast.model.api.responses.MessagesGetConversationMembersResponse
import dev.meloda.fast.model.api.responses.MessagesSendResponse
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.mapApiResult
import dev.meloda.fast.network.service.messages.MessagesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesRepositoryImpl(
    private val messagesService: MessagesService,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val groupDao: GroupDao,
    private val conversationDao: ConversationDao
) : MessagesRepository {

    override suspend fun getHistory(
        conversationId: Long,
        offset: Int?,
        count: Int?
    ): ApiResult<MessagesHistoryInfo, RestApiErrorDomain> = withContext(Dispatchers.IO) {
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

                launch(Dispatchers.IO) {
                    conversationDao.insertAll(conversations.map(VkConversation::asEntity))
                    messageDao.insertAll(messages.map(VkMessage::asEntity))
                    userDao.insertAll(profilesList.map(VkUser::asEntity))
                    groupDao.insertAll(groupsList.map(VkGroupDomain::asEntity))
                }

                MessagesHistoryInfo(
                    messages = messages,
                    conversations = conversations
                )
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun getById(
        peerCmIds: List<Long>?,
        peerId: Long?,
        messagesIds: List<Long>?,
        cmIds: List<Long>?,
        extended: Boolean?,
        fields: String?
    ): ApiResult<List<VkMessage>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesGetByIdRequest(
            peerCmIds = peerCmIds,
            peerId = peerId,
            messagesIds = messagesIds,
            cmIds = cmIds,
            extended = extended,
            fields = fields
        )

        messagesService.getById(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()

                val messages = response.items

                val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
                val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)
                val contactsList = response.contacts.orEmpty().map(VkContactData::mapToDomain)

                val usersMap = VkUsersMap.forUsers(profilesList)
                val groupsMap = VkGroupsMap.forGroups(groupsList)

                val domainMessages = messages.map { message ->
                    message.asDomain().copy(
                        user = usersMap.messageUser(message),
                        group = groupsMap.messageGroup(message),
                        actionUser = usersMap.messageActionUser(message),
                        actionGroup = groupsMap.messageActionGroup(message)
                    )
                }

                launch(Dispatchers.IO) {
                    messageDao.insertAll(domainMessages.map(VkMessage::asEntity))
                    userDao.insertAll(profilesList.map(VkUser::asEntity))
                    groupDao.insertAll(groupsList.map(VkGroupDomain::asEntity))
                }

                VkMemoryCache.appendUsers(profilesList)
                VkMemoryCache.appendGroups(groupsList)
                VkMemoryCache.appendContacts(contactsList)

                domainMessages
            },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun send(
        peerId: Long,
        randomId: Long,
        message: String?,
        replyTo: Long?,
        attachments: List<VkAttachment>?
    ): ApiResult<MessagesSendResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesSendRequest(
            peerId = peerId,
            randomId = randomId,
            message = message,
            replyTo = replyTo,
            attachments = attachments
        )

        messagesService.send(requestModel.map).mapApiDefault()
    }

    override suspend fun markAsRead(
        peerId: Long,
        startMessageId: Long?
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesMarkAsReadRequest(
            peerId = peerId,
            startMessageId = startMessageId
        )

        messagesService.markAsRead(requestModel.map).mapApiDefault()
    }

    override suspend fun getHistoryAttachments(
        peerId: Long,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        cmId: Long
    ): ApiResult<List<VkAttachmentHistoryMessage>, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            val requestModel = MessagesGetHistoryAttachmentsRequest(
                peerId = peerId,
                extended = true,
                count = count,
                offset = offset,
                preserveOrder = true,
                attachmentTypes = attachmentTypes,
                conversationMessageId = cmId,
                fields = VkConstants.ALL_FIELDS
            )

            messagesService.getHistoryAttachments(requestModel.map).mapApiResult(
                successMapper = { apiResponse ->
                    val response = apiResponse.requireResponse()

                    val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
                    val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)
                    val contactsList = response.contacts.orEmpty().map(VkContactData::mapToDomain)

                    VkMemoryCache.appendUsers(profilesList)
                    VkMemoryCache.appendGroups(groupsList)
                    VkMemoryCache.appendContacts(contactsList)

                    launch(Dispatchers.IO) {
                        userDao.insertAll(profilesList.map(VkUser::asEntity))
                        groupDao.insertAll(groupsList.map(VkGroupDomain::asEntity))
                    }

                    response.items.map(VkAttachmentHistoryMessageData::toDomain)
                },
                errorMapper = { error ->
                    error?.toDomain()
                }
            )
        }

    override suspend fun createChat(
        userIds: List<Long>?,
        title: String?
    ): ApiResult<Long, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesCreateChatRequest(
            userIds = userIds,
            title = title
        )

        messagesService.createChat(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                apiResponse.requireResponse().chatId
            },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun pin(
        peerId: Long,
        messageId: Long?,
        cmId: Long?
    ): ApiResult<VkMessage, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesPinMessageRequest(
            peerId = peerId,
            messageId = messageId,
            conversationMessageId = cmId
        )

        messagesService.pin(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                apiResponse.requireResponse().asDomain()
            },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun unpin(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesUnpinMessageRequest(peerId = peerId)
        messagesService.unpin(requestModel.map).mapApiDefault()
    }

    override suspend fun markAsImportant(
        peerId: Long,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        important: Boolean
    ): ApiResult<List<Long>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesMarkAsImportantRequest(
            messagesIds = messageIds.orEmpty(),
            important = important
        )
        messagesService.markAsImportant(requestModel.map).mapApiDefault()
    }

    override suspend fun delete(
        peerId: Long,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        spam: Boolean,
        deleteForAll: Boolean
    ): ApiResult<List<Any>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesDeleteRequest(
            peerId = peerId,
            messagesIds = messageIds,
            conversationsMessagesIds = cmIds,
            isSpam = spam,
            deleteForAll = deleteForAll
        )
        messagesService.delete(requestModel.map).mapApiDefault()
    }

    override suspend fun storeMessages(messages: List<VkMessage>) {
        messageDao.insertAll(messages.map(VkMessage::asEntity))
    }

    override suspend fun edit(
        peerId: Long,
        messageId: Long?,
        cmId: Long?,
        message: String?,
        lat: Float?,
        long: Float?,
        attachments: List<VkAttachment>?,
        notParseLinks: Boolean,
        keepSnippets: Boolean,
        keepForwardedMessages: Boolean
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesEditRequest(
            peerId = peerId,
            messageId = messageId,
            cmId = cmId,
            message = message,
            lat = lat,
            long = long,
            attachments = attachments,
            notParseLinks = notParseLinks,
            keepSnippets = keepSnippets,
            keepForwardedMessages = keepForwardedMessages
        )

        messagesService.edit(requestModel.map).mapApiDefault()
    }

    override suspend fun getChat(
        chatId: Long,
        fields: String?
    ): ApiResult<VkChatData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesGetChatRequest(
            chatId = chatId,
            fields = fields
        )

        messagesService.getChat(requestModel.map).mapApiDefault()
    }

    override suspend fun getConversationMembers(
        peerId: Long,
        offset: Int?,
        count: Int?,
        extended: Boolean?,
        fields: String?
    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            val requestModel = MessagesGetConversationMembersRequest(
                peerId = peerId,
                offset = offset,
                count = count,
                extended = extended,
                fields = fields
            )

            messagesService.getConversationMembers(requestModel.map).mapApiDefault()
        }

    override suspend fun removeChatUser(
        chatId: Long,
        memberId: Long
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesRemoveChatUserRequest(
            chatId = chatId,
            memberId = memberId
        )

        messagesService.removeChatUser(requestModel.map).mapApiDefault()
    }
}
