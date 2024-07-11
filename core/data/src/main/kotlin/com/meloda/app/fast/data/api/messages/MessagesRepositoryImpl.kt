package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.data.VkGroupsMap
import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.data.VkUsersMap
import com.meloda.app.fast.database.dao.MessageDao
import com.meloda.app.fast.model.api.data.VkAttachmentHistoryMessageData
import com.meloda.app.fast.model.api.data.VkContactData
import com.meloda.app.fast.model.api.data.VkGroupData
import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.model.api.data.asDomain
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkAttachmentHistoryMessage
import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.model.api.domain.asEntity
import com.meloda.app.fast.model.api.requests.MessagesGetByIdRequest
import com.meloda.app.fast.model.api.requests.MessagesGetHistoryAttachmentsRequest
import com.meloda.app.fast.model.api.requests.MessagesGetHistoryRequest
import com.meloda.app.fast.model.api.requests.MessagesMarkAsReadRequest
import com.meloda.app.fast.model.api.requests.MessagesSendRequest
import com.meloda.app.fast.network.RestApiErrorDomain
import com.meloda.app.fast.network.mapApiDefault
import com.meloda.app.fast.network.mapApiResult
import com.meloda.app.fast.network.service.messages.MessagesService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesRepositoryImpl(
    private val messagesService: MessagesService,
    private val messageDao: MessageDao,
) : MessagesRepository {

    override suspend fun getHistory(
        conversationId: Int,
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
        messagesIds: List<Int>,
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
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
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
        peerId: Int,
        startMessageId: Int?
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesMarkAsReadRequest(
            peerId = peerId,
            startMessageId = startMessageId
        )

        messagesService.markAsRead(requestModel.map).mapApiDefault()
    }

    override suspend fun getHistoryAttachments(
        peerId: Int,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Int
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

