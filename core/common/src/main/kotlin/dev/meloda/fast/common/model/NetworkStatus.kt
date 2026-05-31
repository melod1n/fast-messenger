package dev.meloda.fast.common.model

enum class NetworkStatus {
    AVAILABLE, UNAVAILABLE, LOST, BLOCKED, UNBLOCKED;

    fun isOk(): Boolean = when (this) {
        AVAILABLE, UNBLOCKED -> true
        UNAVAILABLE, LOST, BLOCKED -> false
    }
}
