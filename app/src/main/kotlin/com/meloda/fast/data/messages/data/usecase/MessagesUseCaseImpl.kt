package com.meloda.fast.data.messages.data.usecase

import com.meloda.fast.api.network.messages.MessagesGetByIdRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdResponse
import com.meloda.fast.api.network.messages.MessagesGetHistoryRequest
import com.meloda.fast.api.network.messages.MessagesGetHistoryResponse
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateApiError
import com.meloda.fast.data.messages.domain.repository.MessagesRepository
import com.meloda.fast.data.messages.domain.usecase.MessagesUseCase
import com.slack.eithernet.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MessagesUseCaseImpl(
    private val messagesRepository: MessagesRepository
) : MessagesUseCase {

    override fun getHistory(
        count: Int?,
        offset: Int?,
        peerId: Int,
        extended: Boolean?,
        startMessageId: Int?,
        rev: Boolean?,
        fields: String?,
    ): Flow<State<MessagesGetHistoryResponse>> = flow {
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
            onSuccess = { response -> State.Success(response) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun getById(
        messagesIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<MessagesGetByIdResponse>> = flow {
        emit(State.Loading)

        val newState = messagesRepository.getById(
            params = MessagesGetByIdRequest(
                messagesIds = messagesIds,
                extended = extended,
                fields = fields
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
}
