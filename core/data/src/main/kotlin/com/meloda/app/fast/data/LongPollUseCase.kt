package com.meloda.app.fast.data

import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
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
