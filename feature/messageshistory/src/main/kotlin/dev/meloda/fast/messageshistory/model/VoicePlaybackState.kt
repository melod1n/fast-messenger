package dev.meloda.fast.messageshistory.model

import androidx.compose.runtime.Immutable

@Immutable
data class VoicePlaybackState(
    val key: String,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long
) {

    val progress: Float
        get() = if (durationMs > 0) {
            (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        } else {
            0f
        }

    companion object {
        val IDLE = VoicePlaybackState(
            key = "",
            isPlaying = false,
            positionMs = 0L,
            durationMs = 0L
        )
    }
}
