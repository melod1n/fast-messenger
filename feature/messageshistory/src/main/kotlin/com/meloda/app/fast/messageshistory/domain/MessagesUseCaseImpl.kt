package com.meloda.app.fast.messageshistory.domain

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.messages.MessagesHistoryDomain
import com.meloda.app.fast.data.api.messages.MessagesRepository
import com.meloda.app.fast.data.api.messages.MessagesUseCase
import com.meloda.app.fast.data.mapToState
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage
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

        val newState = messagesRepository.getMessagesHistory(
            conversationId = conversationId,
            offset = offset,
            count = count
        ).mapToState()

        emit(newState)
    }

    override fun getById(
        messageId: Int,
        extended: Boolean?,
        fields: String?
    ): Flow<State<VkMessage?>> = flow {
        emit(State.Loading)

        val newState = messagesRepository.getMessageById(
            messagesIds = listOf(messageId),
            extended = extended,
            fields = fields
        ).mapToState()
        emit(newState)
    }

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
    ): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = messagesRepository.send(
            peerId = peerId,
            randomId = randomId,
            message = message,
            replyTo = replyTo,
            attachments = attachments
        ).mapToState()

        emit(newState)
    }

    override suspend fun storeMessage(message: VkMessage) {
        messagesRepository.storeMessages(listOf(message))
    }

    override suspend fun storeMessages(messages: List<VkMessage>) {
        messagesRepository.storeMessages(messages)
    }
}
