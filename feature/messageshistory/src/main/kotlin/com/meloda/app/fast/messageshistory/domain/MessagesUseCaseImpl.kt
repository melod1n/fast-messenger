package com.meloda.app.fast.messageshistory.domain

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.messages.MessagesHistoryDomain
import com.meloda.app.fast.data.api.messages.MessagesRepository
import com.meloda.app.fast.data.api.messages.MessagesUseCase
import com.meloda.app.fast.data.toStateApiError
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MessagesUseCaseImpl(
    private val messagesRepository: MessagesRepository
) : MessagesUseCase {

    override fun getMessagesHistory(
        conversationId: Int,
        count: Int?,
        offset: Int?
    ): Flow<State<MessagesHistoryDomain>> = flow {
        emit(State.Loading)

        val newState = when (
            val result = messagesRepository.getMessagesHistory(
                conversationId = conversationId,
                offset = offset,
                count = count
            )
        ) {
            is ApiResult.Success -> State.Success(result.value)

            is ApiResult.Failure.NetworkFailure -> State.Error.ConnectionError
            is ApiResult.Failure.UnknownFailure -> State.UNKNOWN_ERROR
            is ApiResult.Failure.HttpFailure -> result.error.toStateApiError()
            is ApiResult.Failure.ApiFailure -> result.error.toStateApiError()
        }
        emit(newState)
    }
//        messagesRepository.getMessages(conversationId, offset, count).map {
//            State.Success(it)
//        }
//        flow {
//        emit(State.Loading)

//        val newState = messagesRepository.getMessages(
//            conversationId = conversationId,
//            offset = offset,
//            count = count
//        )
//
//        emit(State.Success(newState))
//        ).fold(
//            onSuccess = { response ->
//                // TODO: 05/05/2024, Danil Nikolaev: rewrite
//
//                val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
//                val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)
//
//                VkMemoryCache.appendUsers(profilesList)
//                VkMemoryCache.appendGroups(groupsList)
//
//                val usersMap = VkUsersMap.forUsers(profilesList)
//                val groupsMap = VkGroupsMap.forGroups(groupsList)
//
//                val newMessages = response.items
//                    .map(VkMessageData::mapToDomain)
//                    .map { message ->
//                        val (actionUser, actionGroup) = message.getActionUserAndGroup(
//                            usersMap = usersMap,
//                            groupsMap = groupsMap
//                        )
//
//                        val (messageUser, messageGroup) = message.getUserAndGroup(
//                            usersMap = usersMap,
//                            groupsMap = groupsMap
//                        )
//
//                        message.copy(
//                            user = messageUser,
//                            group = messageGroup,
//                            actionUser = actionUser,
//                            actionGroup = actionGroup
//                        ).also { fullMessage -> VkMemoryCache[fullMessage.id] = fullMessage }
//                    }
//                    .sortedBy(VkMessageDomain::date)
//
//                val conversations = response.conversations?.map { base ->
//                    val lastMessage =
//                        newMessages.find { message -> message.id == base.lastMessageId }
//
//                    base.mapToDomain(lastMessage = lastMessage).run {
//                        val (user, group) = getUserAndGroup(
//                            usersMap = usersMap,
//                            groupsMap = groupsMap
//                        )
//
//                        copy(
//                            conversationUser = user,
//                            conversationGroup = group
//                        ).also { conversation -> VkMemoryCache[conversation.id] = conversation }
//                    }
//                } ?: emptyList()
//
//                storeMessages(newMessages)
//                conversationsUseCase.storeConversations(conversations)

    // TODO: 05/05/2024, Danil Nikolaev: pre-load photos
//                val photos = profilesList.mapNotNull { profile -> profile.photo200 } +
//                        groupsList.mapNotNull { group -> group.photo200 } +
//                        conversations.mapNotNull { conversation -> conversation.avatar?.extractUrl() }

//                photos.forEach { url ->
//                    ImageRequest.Builder(AppGlobal.Instance)
//                        .data(url)
//                        .build()
//                        .let(imageLoader::enqueue)
//                }

//                com.meloda.app.fast.network.State.Success(newMessages)
//            },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }

    override fun getById(
        messageId: Int,
        extended: Boolean?,
        fields: String?
    ): Flow<State<VkMessage?>> = flow {}
//        flow {
//        emit(State.Loading)
//
//        val newState = messagesRepository.getById(
//            params = MessagesGetByIdRequest(
//                messagesIds = listOf(messageId),
//                extended = extended,
//                fields = fields
//            )
//        ).fold(
//            onSuccess = { response ->
//                val message = response.items.singleOrNull()
//                val usersMap =
//                    VkUsersMap.forUsers(response.profiles.orEmpty().map(VkUserData::mapToDomain))
//                val groupsMap =
//                    VkGroupsMap.forGroups(response.groups.orEmpty().map(VkGroupData::mapToDomain))
//
//                com.meloda.app.fast.network.State.Success(
//                    message?.mapToDomain(
//                        user = usersMap.messageUser(message),
//                        group = groupsMap.messageGroup(message),
//                        actionUser = usersMap.messageActionUser(message),
//                        actionGroup = groupsMap.messageActionGroup(message)
//                    )
//                )
//            },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }

    override fun getByIds(
        messageIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessage>>> = flow {}
//        flow {
//        emit(State.Loading)
//
//        val newState = messagesRepository.getById(
//            params = MessagesGetByIdRequest(
//                messagesIds = messageIds,
//                extended = extended,
//                fields = fields
//            )
//        ).fold(
//            onSuccess = { response ->
//                val messages = response.items
//                val usersMap =
//                    VkUsersMap.forUsers(response.profiles.orEmpty().map(VkUserData::mapToDomain))
//                val groupsMap =
//                    VkGroupsMap.forGroups(response.groups.orEmpty().map(VkGroupData::mapToDomain))
//
//                com.meloda.app.fast.network.State.Success(
//                    messages.map { message ->
//                        message.mapToDomain(
//                            user = usersMap.messageUser(message),
//                            group = groupsMap.messageGroup(message),
//                            actionUser = usersMap.messageActionUser(message),
//                            actionGroup = groupsMap.messageActionGroup(message)
//                        )
//                    }
//                )
//            },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }

    override fun sendMessage(
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): Flow<State<Int>> = flow {}
//        flow {
//        emit(State.Loading)
//
//        val newState = messagesRepository.send(
//            MessagesSendRequest(
//                peerId = peerId,
//                randomId = randomId,
//                message = message,
//                replyTo = replyTo,
//                attachments = attachments
//            )
//        ).fold(
//            onSuccess = { response -> com.meloda.app.fast.network.State.Success(response) },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }

    override suspend fun storeMessage(message: VkMessage) {
        messagesRepository.storeMessages(listOf(message))
    }

    override suspend fun storeMessages(messages: List<VkMessage>) {
        messagesRepository.storeMessages(messages)
    }
}
