package com.meloda.fast.service.longpolling.data.usecase

import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.messages.MessagesGetLongPollServerRequest
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateApiError
import com.meloda.fast.service.longpolling.data.LongPollUpdates
import com.meloda.fast.service.longpolling.domain.repository.LongPollRepository
import com.meloda.fast.service.longpolling.domain.usecase.LongPollUseCase
import com.slack.eithernet.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LongPollUseCaseImpl(private val longPollRepository: LongPollRepository) : LongPollUseCase {

    override fun getLongPollServer(
        needPts: Boolean,
        version: Int
    ): Flow<State<VkLongPollData>> = flow {
        emit(State.Loading)

        val newState = longPollRepository.getLongPollServer(
            params = MessagesGetLongPollServerRequest(
                needPts = needPts,
                version = version
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

    override fun getLongPollUpdates(
        serverUrl: String,
        act: String,
        key: String,
        ts: Int,
        wait: Int,
        mode: Int,
        version: Int
    ): Flow<State<LongPollUpdates>> = flow {
        emit(State.Loading)

        val newState = longPollRepository.getLongPollUpdates(
            serverUrl = serverUrl,
            params = LongPollGetUpdatesRequest(
                act = act,
                key = key,
                ts = ts,
                wait = wait,
                mode = mode,
                version = version
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
