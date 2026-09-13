package dev.meloda.fast.messageshistory.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dev.meloda.fast.messageshistory.model.VoicePlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VoiceMessagesPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onReleased: () -> Unit = {}
) {

    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(VoicePlaybackState.IDLE)
    val state: StateFlow<VoicePlaybackState> = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_ENDED -> {
                    _state.value = VoicePlaybackState.IDLE
                    stopProgressUpdates()
                    releasePlayer()
                }

                Player.STATE_READY -> {
                    val player = exoPlayer ?: return
                    val duration = player.duration.coerceAtLeast(0L)
                    _state.update {
                        it.copy(
                            durationMs = if (duration > 0) duration else it.durationMs,
                            isPlaying = player.isPlaying
                        )
                    }
                }

                Player.STATE_IDLE, Player.STATE_BUFFERING -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = VoicePlaybackState.IDLE
            stopProgressUpdates()
            releasePlayer()
        }
    }

    fun toggle(url: String) {
        if (url.isBlank()) return

        if (_state.value.key == url) {
            val player = exoPlayer
            if (player != null) {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            } else {
                playNew(url)
            }
        } else {
            playNew(url)
        }
    }

    fun stop() {
        releaseInternal()
    }

    fun release() {
        releaseInternal()
        onReleased()
    }

    private fun playNew(url: String) {
        releaseInternal()

        try {
            val player = ExoPlayer.Builder(context).build().apply {
                addListener(playerListener)
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
            }

            exoPlayer = player
            _state.value = VoicePlaybackState(
                key = url,
                isPlaying = true,
                positionMs = 0L,
                durationMs = 0L
            )
            startProgressUpdates()
        } catch (_: Exception) {
            releaseInternal()
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                delay(PROGRESS_UPDATE_INTERVAL_MS)
                val player = exoPlayer ?: break
                if (!player.isPlaying) continue

                _state.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L).takeIf { d -> d > 0 } ?: it.durationMs
                    )
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            player.removeListener(playerListener)
            player.stop()
            player.release()
        }
        exoPlayer = null
    }

    private fun releaseInternal() {
        stopProgressUpdates()
        releasePlayer()
        _state.value = VoicePlaybackState.IDLE
    }

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL_MS = 100L
    }
}
