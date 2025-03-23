package dev.meloda.fast.model

enum class LongPollEvent {
    MESSAGE_SET_FLAGS,
    MESSAGE_CLEAR_FLAGS,
    MESSAGE_NEW,
    MESSAGE_EDITED,
    INCOMING_MESSAGE_READ,
    OUTGOING_MESSAGE_READ,
    CHAT_SET_FLAGS,
    CHAT_CLEAR_FLAGS,
    CHAT_MAJOR_CHANGED,
    CHAT_MINOR_CHANGED,
    TYPING,
    AUDIO_MESSAGE_RECORDING,
    PHOTO_UPLOADING,
    VIDEO_UPLOADING,
    FILE_UPLOADING,
    UNREAD_COUNTER_UPDATE,
    MARKED_AS_IMPORTANT,
    MARKED_AS_SPAM,
    MARKED_AS_NOT_SPAM,
    MESSAGE_DELETED,
    MESSAGE_RESTORED,
    AUDIO_MESSAGE_LISTENED,
    CHAT_CLEARED
}
