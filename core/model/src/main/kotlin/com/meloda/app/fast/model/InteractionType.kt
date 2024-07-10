package com.meloda.app.fast.model

sealed class InteractionType(val value: Int) {
    data object Typing : InteractionType(1)
    data object VoiceMessage : InteractionType(2)
    data object Photo : InteractionType(3)
    data object Video : InteractionType(4)
    data object File : InteractionType(5)

    companion object {
        fun parse(value: Int): InteractionType? = when (value) {
            1 -> Typing
            2 -> VoiceMessage
            3 -> Photo
            4 -> Video
            5 -> File
            else -> null
        }
    }
}
