package dev.meloda.fast.common

import kotlinx.coroutines.flow.MutableStateFlow

object ActiveChatTracker {

    private val activeChatPeerId = MutableStateFlow<Long?>(null)
    private val isAppInForeground = MutableStateFlow(false)

    fun onAppStarted() {
        isAppInForeground.value = true
    }

    fun onAppStopped() {
        isAppInForeground.value = false
    }

    fun onChatOpened(peerId: Long) {
        activeChatPeerId.value = peerId
    }

    fun onChatClosed(peerId: Long) {
        if (activeChatPeerId.value == peerId) {
            activeChatPeerId.value = null
        }
    }

    fun isActiveForegroundChat(peerId: Long): Boolean =
        isAppInForeground.value && activeChatPeerId.value == peerId
}
