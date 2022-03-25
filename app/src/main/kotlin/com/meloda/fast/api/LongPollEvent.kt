package com.meloda.fast.api

import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser

sealed class LongPollEvent {

    data class VkMessageNewEvent(
        val message: VkMessage,
        val profiles: HashMap<Int, VkUser>,
        val groups: HashMap<Int, VkGroup>
    ) : LongPollEvent()

    data class VkMessageEditEvent(val message: VkMessage) : LongPollEvent()

    data class VkMessageReadIncomingEvent(val peerId: Int, val messageId: Int) : LongPollEvent()
    data class VkMessageReadOutgoingEvent(val peerId: Int, val messageId: Int) : LongPollEvent()

}