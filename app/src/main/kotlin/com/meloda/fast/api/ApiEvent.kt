package com.meloda.fast.api

enum class ApiEvent(val value: Int) {
    MessageSetFlags(2),
    MessageClearFlags(3),
    MessageNew(4),
    MessageEdit(5),
    MessageReadIncoming(6),
    MessageReadOutgoing(7),
    MessagesDeleted(13),
    PinUnpinConversation(20),
    Typing(63),
    VoiceRecording(64),
    PhotoUploading(65),
    VideoUploading(66),
    FileUploading(67),
    UnreadCountUpdate(80)
    ;

    companion object {
        fun parse(value: Int) = entries.firstOrNull { it.value == value }
    }
}
