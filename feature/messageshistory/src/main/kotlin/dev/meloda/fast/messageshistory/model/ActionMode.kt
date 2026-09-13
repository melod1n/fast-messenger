package dev.meloda.fast.messageshistory.model

enum class ActionMode {
    SEND,
    RECORD_AUDIO,
    RECORD_VIDEO,
    RECORDING,
    EDIT,
    DELETE;

    fun isRecord(): Boolean = this == RECORD_AUDIO || this == RECORD_VIDEO || this == RECORDING
}
