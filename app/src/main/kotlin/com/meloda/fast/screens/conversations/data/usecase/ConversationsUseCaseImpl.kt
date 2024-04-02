package com.meloda.fast.screens.conversations.data.usecase

import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsPinRequest
import com.meloda.fast.api.network.conversations.ConversationsResponseDomain
import com.meloda.fast.api.network.conversations.ConversationsUnpinRequest
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateApiError
import com.meloda.fast.screens.conversations.domain.repository.ConversationsRepository
import com.meloda.fast.screens.conversations.domain.usecase.ConversationsUseCase
import com.slack.eithernet.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ConversationsUseCaseImpl(
    private val conversationsRepository: ConversationsRepository
) : ConversationsUseCase {

    override fun getConversations(
        count: Int?,
        offset: Int?,
        fields: String,
        filter: String,
        extended: Boolean?,
        startMessageId: Int?
    ): Flow<State<ConversationsResponseDomain>> = flow {
        emit(State.Loading)

        val newState = conversationsRepository.getConversations(
            params = ConversationsGetRequest(
                count = count,
                offset = offset,
                fields = fields,
                filter = filter,
                extended = extended,
                startMessageId = startMessageId
            )
        ).fold(
            onSuccess = { response -> State.Success(response.toDomain()) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun delete(peerId: Int): Flow<State<Unit>> = flow {
        emit(State.Loading)

        val newState = conversationsRepository.delete(
            ConversationsDeleteRequest(peerId = peerId)
        ).fold(
            onSuccess = { State.Success(Unit) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun pin(peerId: Int): Flow<State<Unit>> = flow {
        emit(State.Loading)

        val newState = conversationsRepository.pin(
            ConversationsPinRequest(peerId = peerId)
        ).fold(
            onSuccess = { State.Success(Unit) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun unpin(peerId: Int): Flow<State<Unit>> = flow {
        emit(State.Loading)

        val newState = conversationsRepository.unpin(
            ConversationsUnpinRequest(peerId = peerId)
        ).fold(
            onSuccess = { State.Success(Unit) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }
}
