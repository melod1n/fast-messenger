package com.meloda.app.fast.service.longpolling

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.longpoll.LongPollRepository
import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// TODO: 05/05/2024, Danil Nikolaev: implement
class LongPollUseCaseImpl(
    private val longPollRepository: LongPollRepository
) : LongPollUseCase {

    override fun getLongPollServer(
        needPts: Boolean,
        version: Int
    ): Flow<State<VkLongPollData>> = flow {
//        emit(State.Loading)
//
//        val newState = longPollRepository.getLongPollServer(
//            params = MessagesGetLongPollServerRequest(
//                needPts = needPts,
//                version = version
//            )
//        ).fold(
//            onSuccess = { response -> State.Success(response) },
//            onNetworkFailure = { State.Error.ConnectionError },
//            onUnknownFailure = { State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
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
//        emit(State.Loading)
//
//        val newState = longPollRepository.getLongPollUpdates(
//            serverUrl = serverUrl,
//            params = LongPollGetUpdatesRequest(
//                act = act,
//                key = key,
//                ts = ts,
//                wait = wait,
//                mode = mode,
//                version = version
//            )
//        ).fold(
//            onSuccess = { response -> State.Success(response) },
//            onNetworkFailure = { State.Error.ConnectionError },
//            onUnknownFailure = { State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
    }
}
