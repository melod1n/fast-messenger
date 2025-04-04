package dev.meloda.fast.model

enum class ApiEvent(val value: Int) {
    MESSAGE_SET_FLAGS(10002),
    MESSAGE_CLEAR_FLAGS(10003),
    MESSAGE_NEW(10004),
    MESSAGE_EDIT(10005),
    MESSAGE_READ_INCOMING(10006),
    MESSAGE_READ_OUTGOING(10007),
    CHAT_CLEAR_FLAGS(10),
    CHAT_SET_FLAGS(12),
    MESSAGES_DELETED(10013),
    MESSAGE_UPDATED(10018),
    MESSAGE_CACHE_CLEAR(10019),
    CHAT_MAJOR_CHANGED(20),
    CHAT_MINOR_CHANGED(21),
    TYPING(63),
    AUDIO_MESSAGE_RECORDING(64),
    PHOTO_UPLOADING(65),
    VIDEO_UPLOADING(66),
    FILE_UPLOADING(67),
    UNREAD_COUNT_UPDATE(80);

    companion object {
        fun parse(value: Int) = entries.first { it.value == value }
        fun parseOrNull(value: Int) = entries.firstOrNull { it.value == value }
    }
}
