package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.model.api.data.LongPollUpdates
import dev.meloda.fast.model.api.data.VkLongPollData
import kotlinx.coroutines.flow.Flow

interface LongPollUseCase {

    fun getLongPollServer(
        needPts: Boolean,
        version: Int
    ): Flow<State<VkLongPollData>>

    fun getLongPollUpdates(
        serverUrl: String,
        act: String = "a_check",
        key: String,
        ts: Int,
        wait: Int,
        mode: Int,
        version: Int
    ): Flow<State<LongPollUpdates>>
}
