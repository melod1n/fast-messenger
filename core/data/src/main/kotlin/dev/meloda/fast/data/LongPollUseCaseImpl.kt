package dev.meloda.fast.data

import dev.meloda.fast.data.api.longpoll.LongPollRepository
import dev.meloda.fast.model.api.data.LongPollUpdates
import dev.meloda.fast.model.api.data.VkLongPollData
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
