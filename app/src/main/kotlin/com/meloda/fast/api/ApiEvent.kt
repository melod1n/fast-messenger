package com.meloda.fast.api

enum class ApiEvent(val value: Int) {
    MESSAGE_SET_FLAGS(2),
    MESSAGE_CLEAR_FLAGS(3),
    MESSAGE_NEW(4),
    MESSAGE_EDIT(5),
    MESSAGE_READ_INCOMING(6),
    MESSAGE_READ_OUTGOING(7),
    FRIEND_ONLINE(8),
    FRIEND_OFFLINE(9),
    MESSAGES_DELETED(13),
    PIN_UNPIN_CONVERSATION(20),
    PRIVATE_TYPING(61),
    CHAT_TYPING(62),
    ONE_MORE_TYPING(63),
    VOICE_RECORDING(64),
    PHOTO_UPLOADING(65),
    VIDEO_UPLOADING(66),
    FILE_UPLOADING(67),
    UNREAD_COUNT_UPDATE(80)
    ;

    companion object {
        fun parse(value: Int) = values().firstOrNull { it.value == value }
    }

}