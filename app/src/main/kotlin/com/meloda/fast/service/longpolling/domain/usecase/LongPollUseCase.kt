package com.meloda.fast.service.longpolling.domain.usecase

import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.base.State
import com.meloda.fast.service.longpolling.data.LongPollUpdates
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
