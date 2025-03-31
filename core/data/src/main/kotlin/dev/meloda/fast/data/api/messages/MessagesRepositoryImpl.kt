package dev.meloda.fast.data.api.messages

import com.slack.eithernet.ApiResult
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.VkGroupsMap
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.VkUsersMap
import dev.meloda.fast.database.dao.MessageDao
import dev.meloda.fast.model.api.data.VkAttachmentHistoryMessageData
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkGroupData
import dev.meloda.fast.model.api.data.VkUserData
import dev.meloda.fast.model.api.data.asDomain
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.asEntity
import dev.meloda.fast.model.api.requests.MessagesCreateChatRequest
import dev.meloda.fast.model.api.requests.MessagesDeleteRequest
import dev.meloda.fast.model.api.requests.MessagesGetByIdRequest
import dev.meloda.fast.model.api.requests.MessagesGetHistoryAttachmentsRequest
import dev.meloda.fast.model.api.requests.MessagesGetHistoryRequest
import dev.meloda.fast.model.api.requests.MessagesMarkAsImportantRequest
import dev.meloda.fast.model.api.requests.MessagesMarkAsReadRequest
import dev.meloda.fast.model.api.requests.MessagesPinMessageRequest
import dev.meloda.fast.model.api.requests.MessagesSendRequest
import dev.meloda.fast.model.api.requests.MessagesUnpinMessageRequest
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.mapApiResult
import dev.meloda.fast.network.service.messages.MessagesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesRepositoryImpl(
    private val messagesService: MessagesService,
    private val messageDao: MessageDao,
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
        messagesIds: List<Long>,
        extended: Boolean?,
        fields: String?
    ): ApiResult<List<VkMessage>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesGetByIdRequest(
            messagesIds = messagesIds,
            extended = extended,
            fields = fields
        )

        messagesService.getById(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()

                val messages = response.items
                val usersMap =
                    VkUsersMap.forUsers(response.profiles.orEmpty().map(VkUserData::mapToDomain))
                val groupsMap =
                    VkGroupsMap.forGroups(response.groups.orEmpty().map(VkGroupData::mapToDomain))

                messages.map { message ->
                    message.asDomain().copy(
                        user = usersMap.messageUser(message),
                        group = groupsMap.messageGroup(message),
                        actionUser = usersMap.messageActionUser(message),
                        actionGroup = groupsMap.messageActionGroup(message)
                    )
                }
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
    ): ApiResult<Long, RestApiErrorDomain> = withContext(Dispatchers.IO) {
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
        conversationMessageId: Long
    ): ApiResult<List<VkAttachmentHistoryMessage>, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            val requestModel = MessagesGetHistoryAttachmentsRequest(
                peerId = peerId,
                extended = true,
                count = count,
                offset = offset,
                preserveOrder = true,
                attachmentTypes = attachmentTypes,
                conversationMessageId = conversationMessageId,
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
        conversationMessageId: Long?
    ): ApiResult<VkMessage, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesPinMessageRequest(
            peerId = peerId,
            messageId = messageId,
            conversationMessageId = conversationMessageId
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
        conversationMessageIds: List<Long>?,
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
        conversationMessageIds: List<Long>?,
        spam: Boolean,
        deleteForAll: Boolean
    ): ApiResult<List<Any>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesDeleteRequest(
            peerId = peerId,
            messagesIds = messageIds,
            conversationsMessagesIds = conversationMessageIds,
            isSpam = spam,
            deleteForAll = deleteForAll
        )
        messagesService.delete(requestModel.map).mapApiDefault()
    }

    override suspend fun storeMessages(messages: List<VkMessage>) {
        messageDao.insertAll(messages.map(VkMessage::asEntity))
    }

//    override suspend fun markAsImportant(
//        params: MessagesMarkAsImportantRequest
//    ): ApiResult<List<Int>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.markAsImportant(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
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
