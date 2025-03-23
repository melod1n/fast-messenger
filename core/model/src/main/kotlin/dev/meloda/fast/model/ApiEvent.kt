package dev.meloda.fast.model

enum class ApiEvent(val value: Int) {
    MESSAGE_SET_FLAGS(2),
    MESSAGE_CLEAR_FLAGS(3),
    MESSAGE_NEW(4),
    MESSAGE_EDIT(5),
    MESSAGE_READ_INCOMING(6),
    MESSAGE_READ_OUTGOING(7),
    CHAT_CLEAR_FLAGS(10),
    CHAT_SET_FLAGS(12),
    MESSAGES_DELETED(13),
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
