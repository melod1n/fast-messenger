package dev.meloda.fast.messageshistory.presentation

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dev.meloda.fast.model.api.domain.VkVideoMessageDomain
import dev.meloda.fast.ui.R
import kotlinx.coroutines.delay

@Composable
fun VideoMessagePlayerDialog(
    videoMessage: VkVideoMessageDomain?,
    onDismiss: () -> Unit
) {
    if (videoMessage == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        RememberImmersiveWindow()

        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val screenSize = minOf(configuration.screenWidthDp, configuration.screenHeightDp).dp

        var isPlaying by rememberSaveable { mutableStateOf(true) }
        var isMuted by rememberSaveable { mutableStateOf(false) }
        var isExpanded by rememberSaveable { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0f) }
        var isBuffering by remember { mutableStateOf(false) }

        val exoPlayer = remember(videoMessage.link) {
            videoMessage.link?.let { url ->
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("okhttp/4.12.0")
                    .setAllowCrossProtocolRedirects(true)
                val mediaSourceFactory = DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(dataSourceFactory)

                val mediaItem = if (url.contains(".m3u8") || url.contains("/hls/")) {
                    MediaItem.Builder()
                        .setUri(url)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                } else {
                    MediaItem.fromUri(url)
                }

                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build().apply {
                        setMediaItem(mediaItem)
                        repeatMode = ExoPlayer.REPEAT_MODE_ONE
                        prepare()
                        playWhenReady = true
                    }
            }
        }

        DisposableEffect(exoPlayer) {
            onDispose {
                exoPlayer?.release()
            }
        }

        LaunchedEffect(exoPlayer, isMuted) {
            exoPlayer?.volume = if (isMuted) 0f else 1f
        }

        LaunchedEffect(exoPlayer) {
            while (exoPlayer != null) {
                val duration = exoPlayer.duration
                if (duration > 0) {
                    progress = (exoPlayer.currentPosition.toFloat() / duration).coerceIn(0f, 1f)
                }
                isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
                delay(100L)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            if (exoPlayer != null) {
                val videoSurfaceModifier = Modifier
                    .size(if (isExpanded) screenSize else (screenSize * 0.75f))
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                                isPlaying = false
                            } else {
                                exoPlayer.play()
                                isPlaying = true
                            }
                        }
                    )

                if (isExpanded) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = videoSurfaceModifier) {
                            VideoMessagePlayerView(exoPlayer = exoPlayer)

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }

                        if (!isPlaying) {
                            PlayOverlay()
                        } else if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier.size(screenSize * 0.75f + 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 3.dp
                            )

                            Box(modifier = videoSurfaceModifier) {
                                VideoMessagePlayerView(exoPlayer = exoPlayer)
                            }
                        }

                        if (!isPlaying) {
                            PlayOverlay()
                        } else if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
            } else {
                Text(text = "Видеосообщение недоступно", color = Color.White)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_round_24),
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (exoPlayer != null) {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            painter = painterResource(
                                if (isExpanded) {
                                    R.drawable.ic_fullscreen_exit_round_24
                                } else {
                                    R.drawable.ic_fullscreen_round_24
                                }
                            ),
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color.White
                        )
                    }
                }
            }

            if (exoPlayer != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isMuted) {
                                    R.drawable.ic_volume_off_round_24
                                } else {
                                    R.drawable.ic_volume_up_round_24
                                }
                            ),
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    if (videoMessage.duration > 0) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${videoMessage.duration / 60}:${(videoMessage.duration % 60).toString().padStart(2, '0')}",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoMessagePlayerView(exoPlayer: ExoPlayer) {
    AndroidView(
        factory = { factoryContext ->
            TextureView(factoryContext).also { textureView ->
                exoPlayer.setVideoTextureView(textureView)
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { textureView ->
            exoPlayer.clearVideoTextureView(textureView)
        }
    )
}

@Composable
private fun PlayOverlay() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play_arrow_fill_round_24),
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}
