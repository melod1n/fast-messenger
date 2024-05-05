package com.meloda.fast.screens.messages.data.usecase

import com.meloda.fast.api.VkGroupsMap
import com.meloda.fast.api.VkMemoryCache
import com.meloda.fast.api.VkUsersMap
import com.meloda.fast.api.model.data.VkGroupData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.model.domain.VkAttachment
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.network.messages.MessagesGetByIdRequest
import com.meloda.fast.api.network.messages.MessagesGetHistoryRequest
import com.meloda.fast.api.network.messages.MessagesSendRequest
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateApiError
import com.meloda.fast.database.dao.MessagesDao
import com.meloda.fast.screens.conversations.domain.usecase.ConversationsUseCase
import com.meloda.fast.screens.messages.domain.repository.MessagesRepository
import com.meloda.fast.screens.messages.domain.usecase.MessagesUseCase
import com.slack.eithernet.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MessagesUseCaseImpl(
    private val messagesRepository: MessagesRepository,
    private val messagesDao: MessagesDao,
    private val conversationsUseCase: ConversationsUseCase
) : MessagesUseCase {

    override fun getHistory(
        count: Int?,
        offset: Int?,
        peerId: Int,
        extended: Boolean?,
        startMessageId: Int?,
        rev: Boolean?,
        fields: String?,
    ): Flow<State<List<VkMessageDomain>>> = flow {
        emit(State.Loading)

        val newState = messagesRepository.getHistory(
            params = MessagesGetHistoryRequest(
                count = count,
                offset = offset,
                peerId = peerId,
                extended = extended,
                startMessageId = startMessageId,
                rev = rev,
                fields = fields
            )
        ).fold(
            onSuccess = { response ->
                // TODO: 05/05/2024, Danil Nikolaev: rewrite

                val profilesList = response.profiles.orEmpty().map(VkUserData::mapToDomain)
                val groupsList = response.groups.orEmpty().map(VkGroupData::mapToDomain)

                VkMemoryCache.appendUsers(profilesList)
                VkMemoryCache.appendGroups(groupsList)

                val usersMap = VkUsersMap.forUsers(profilesList)
                val groupsMap = VkGroupsMap.forGroups(groupsList)

                val newMessages = response.items
                    .map(VkMessageData::mapToDomain)
                    .map { message ->
                        val (actionUser, actionGroup) = message.getActionUserAndGroup(
                            usersMap = usersMap,
                            groupsMap = groupsMap
                        )

                        val (messageUser, messageGroup) = message.getUserAndGroup(
                            usersMap = usersMap,
                            groupsMap = groupsMap
                        )

                        message.copy(
                            user = messageUser,
                            group = messageGroup,
                            actionUser = actionUser,
                            actionGroup = actionGroup
                        ).also { fullMessage -> VkMemoryCache[fullMessage.id] = fullMessage }
                    }
                    .sortedBy(VkMessageDomain::date)

                val conversations = response.conversations?.map { base ->
                    val lastMessage =
                        newMessages.find { message -> message.id == base.lastMessageId }

                    base.mapToDomain(lastMessage = lastMessage).run {
                        val (user, group) = getUserAndGroup(
                            usersMap = usersMap,
                            groupsMap = groupsMap
                        )

                        copy(
                            conversationUser = user,
                            conversationGroup = group
                        ).also { conversation -> VkMemoryCache[conversation.id] = conversation }
                    }
                } ?: emptyList()

                storeMessages(newMessages)
                conversationsUseCase.storeConversations(conversations)

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

                State.Success(newMessages)
            },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun getById(
        messageId: Int,
        extended: Boolean?,
        fields: String?
    ): Flow<State<VkMessageDomain?>> = flow {
        emit(State.Loading)

        val newState = messagesRepository.getById(
            params = MessagesGetByIdRequest(
                messagesIds = listOf(messageId),
                extended = extended,
                fields = fields
            )
        ).fold(
            onSuccess = { response ->
                val message = response.items.singleOrNull()
                val usersMap =
                    VkUsersMap.forUsers(response.profiles.orEmpty().map(VkUserData::mapToDomain))
                val groupsMap =
                    VkGroupsMap.forGroups(response.groups.orEmpty().map(VkGroupData::mapToDomain))

                State.Success(
                    message?.mapToDomain(
                        user = usersMap.messageUser(message),
                        group = groupsMap.messageGroup(message),
                        actionUser = usersMap.messageActionUser(message),
                        actionGroup = groupsMap.messageActionGroup(message)
                    )
                )
            },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun getByIds(
        messageIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessageDomain>>> = flow {
        emit(State.Loading)

        val newState = messagesRepository.getById(
            params = MessagesGetByIdRequest(
                messagesIds = messageIds,
                extended = extended,
                fields = fields
            )
        ).fold(
            onSuccess = { response ->
                val messages = response.items
                val usersMap =
                    VkUsersMap.forUsers(response.profiles.orEmpty().map(VkUserData::mapToDomain))
                val groupsMap =
                    VkGroupsMap.forGroups(response.groups.orEmpty().map(VkGroupData::mapToDomain))

                State.Success(
                    messages.map { message ->
                        message.mapToDomain(
                            user = usersMap.messageUser(message),
                            group = groupsMap.messageGroup(message),
                            actionUser = usersMap.messageActionUser(message),
                            actionGroup = groupsMap.messageActionGroup(message)
                        )
                    }
                )
            },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun sendMessage(
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = messagesRepository.send(
            MessagesSendRequest(
                peerId = peerId,
                randomId = randomId,
                message = message,
                replyTo = replyTo,
                attachments = attachments
            )
        ).fold(
            onSuccess = { response -> State.Success(response) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override suspend fun storeMessage(message: VkMessageDomain) {
        messagesDao.insert(message.mapToDB())
    }

    override suspend fun storeMessages(messages: List<VkMessageDomain>) {
        messagesDao.insertAll(messages.map(VkMessageDomain::mapToDB))
    }
}
