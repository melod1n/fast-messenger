package com.meloda.app.fast.data

import com.meloda.app.fast.data.api.longpoll.LongPollRepository
import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LongPollUseCaseImpl(
    private val repository: LongPollRepository
) : LongPollUseCase {

    override fun getLongPollServer(
        needPts: Boolean,
        version: Int
    ): Flow<State<VkLongPollData>> = flow {
        emit(State.Loading)

        val newState = repository.getLongPollServer(
            needPts = needPts,
            version = version
        ).mapToState()

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

        val newState = repository.getLongPollUpdates(
            serverUrl,
            act = act,
            key = key,
            ts = ts,
            wait = wait,
            mode = mode,
            version = version
        ).mapToState()
        emit(newState)
    }
}
