package dev.meloda.fast.messageshistory.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.slack.eithernet.ApiResult
import dev.meloda.fast.data.api.videos.VideosRepository
import dev.meloda.fast.model.api.data.VkVideoData
import dev.meloda.fast.model.api.domain.VkVideoDomain
import dev.meloda.fast.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.compose.getKoin

private data class VideoQuality(
    val label: String,
    val url: String
)

@Composable
fun VideoPlayerDialog(
    video: VkVideoDomain?,
    onDismiss: () -> Unit
) {
    if (video == null) return

    val context = LocalContext.current
    val koin = getKoin()
    val videosRepository = remember { runCatching { koin.get<VideosRepository>() }.getOrNull() }

    val vkWebUrl =
        "https://vk.com/video${video.ownerId}_${video.id}${video.accessKey?.let { "_$it" }.orEmpty()}"
    var streamUrl by remember(video) { mutableStateOf(video.directUrl) }
    var qualities by remember(video) { mutableStateOf<List<VideoQuality>>(emptyList()) }
    var isLoadingDetails by remember(video) { mutableStateOf(streamUrl.isNullOrBlank()) }
    var savedPosition by rememberSaveable(video) { mutableStateOf(0L) }
    var isQualityMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(video) {
        if (videosRepository != null) {
            isLoadingDetails = streamUrl.isNullOrBlank()
            val videoQuery = "${video.ownerId}_${video.id}${video.accessKey?.let { "_$it" } ?: ""}"
            withContext(Dispatchers.IO) {
                runCatching {
                    when (val result = videosRepository.get(videoQuery)) {
                        is ApiResult.Success -> {
                            val item = result.value.response?.items?.firstOrNull()
                            val availableQualities = item?.files?.let { files ->
                                buildVideoQualities(files)
                            }.orEmpty()

                            withContext(Dispatchers.Main) {
                                qualities = availableQualities
                                if (streamUrl.isNullOrBlank()) {
                                    streamUrl = availableQualities.firstOrNull()?.url
                                }
                                isLoadingDetails = false
                            }
                        }

                        is ApiResult.Failure -> {
                            withContext(Dispatchers.Main) {
                                isLoadingDetails = false
                            }
                        }
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        isLoadingDetails = false
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        RememberImmersiveWindow()

        val exoPlayer = remember(streamUrl) {
            if (!streamUrl.isNullOrBlank()) {
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("okhttp/4.12.0")
                    .setAllowCrossProtocolRedirects(true)
                val mediaSourceFactory = DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(dataSourceFactory)

                val mediaItem = if (streamUrl!!.contains(".m3u8") || streamUrl!!.contains("/hls/")) {
                    MediaItem.Builder()
                        .setUri(streamUrl)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                } else {
                    MediaItem.fromUri(streamUrl!!)
                }

                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build().apply {
                        setMediaItem(mediaItem)
                        prepare()
                        playWhenReady = true
                        if (savedPosition > 0L) {
                            seekTo(savedPosition)
                        }
                    }
            } else {
                null
            }
        }

        DisposableEffect(exoPlayer) {
            onDispose {
                exoPlayer?.let { player ->
                    val position = player.currentPosition
                    if (position > 0L) {
                        savedPosition = position
                    }
                    player.release()
                }
            }
        }

        LaunchedEffect(exoPlayer) {
            while (exoPlayer != null) {
                val position = exoPlayer.currentPosition
                if (position > 0L) {
                    savedPosition = position
                }
                delay(500L)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (exoPlayer != null) {
                AndroidView(
                    factory = { factoryContext ->
                        PlayerView(factoryContext).apply {
                            useController = true
                            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                            player = exoPlayer
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        view.player = exoPlayer
                    }
                )
            } else if (isLoadingDetails) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Загрузка видео...", color = Color.White, fontSize = 14.sp)
                }
            } else {
                val previewUrl = video.getDefault()?.url
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!previewUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.DarkGray)
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, vkWebUrl.toUri()))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = previewUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_play_arrow_fill_round_24),
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Text(
                        text = video.title.ifBlank { "Видеозапись" },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Воспроизведение доступно через сервис VK Video",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, vkWebUrl.toUri()))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_arrow_fill_round_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Смотреть видео")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_round_24),
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Text(
                    text = video.title.ifBlank { "Видео" },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                val currentQualityLabel = qualities
                    .find { it.url == streamUrl }
                    ?.label

                if (qualities.size > 1) {
                    Box {
                        Text(
                            text = currentQualityLabel ?: "Качество",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable { isQualityMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )

                        DropdownMenu(
                            expanded = isQualityMenuExpanded,
                            onDismissRequest = { isQualityMenuExpanded = false }
                        ) {
                            qualities.forEach { quality ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = quality.label,
                                            fontWeight = if (quality.url == streamUrl) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                    },
                                    onClick = {
                                        isQualityMenuExpanded = false
                                        if (quality.url != streamUrl) {
                                            exoPlayer?.let { player ->
                                                val position = player.currentPosition
                                                if (position > 0L) {
                                                    savedPosition = position
                                                }
                                            }
                                            streamUrl = quality.url
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, vkWebUrl.toUri()))
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_video_fill_round_24),
                        contentDescription = "Open in browser or VK",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun buildVideoQualities(files: VkVideoData.File): List<VideoQuality> {
    return buildList {
        files.mp41440?.let { add(VideoQuality("1440p", it)) }
        files.mp41080?.let { add(VideoQuality("1080p", it)) }
        files.mp4720?.let { add(VideoQuality("720p", it)) }
        files.mp4480?.let { add(VideoQuality("480p", it)) }
        files.mp4360?.let { add(VideoQuality("360p", it)) }
        files.mp4240?.let { add(VideoQuality("240p", it)) }
        files.hls?.let { add(VideoQuality("Авто", it)) }
    }
}
