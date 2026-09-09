package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.meloda.fast.messageshistory.model.VoicePlaybackState
import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAudioDomain
import dev.meloda.fast.model.api.domain.VkAudioMessageDomain
import dev.meloda.fast.model.api.domain.VkFileDomain
import dev.meloda.fast.model.api.domain.VkGiftDomain
import dev.meloda.fast.model.api.domain.VkLinkDomain
import dev.meloda.fast.model.api.domain.VkPhotoDomain
import dev.meloda.fast.model.api.domain.VkStickerDomain
import dev.meloda.fast.model.api.domain.VkVideoDomain
import dev.meloda.fast.model.api.domain.VkVideoMessageDomain
import dev.meloda.fast.common.ImmutableList
import dev.meloda.fast.common.ImmutableList.Companion.toImmutableList

private val previewTypes = listOf(
    AttachmentType.PHOTO,
    AttachmentType.VIDEO
)

@Composable
fun Attachments(
    withText: Boolean,
    withReply: Boolean,
    modifier: Modifier = Modifier,
    attachments: ImmutableList<out VkAttachment>,
    voicePlayback: VoicePlaybackState = VoicePlaybackState.IDLE,
    onClick: (VkAttachment) -> Unit = {},
    onLongClick: (VkAttachment) -> Unit = {}
) {
    if (attachments.isEmpty()) return

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    Column(modifier = modifier) {
        val previewAttachments = remember(attachments) {
            attachments.values.filter { it.type in previewTypes }
        }

        val nonPreviewAttachments = remember(attachments) {
            attachments.values.filterNot { it.type in previewTypes }.sortedBy { it.type.ordinal }
        }

        if (previewAttachments.isNotEmpty()) {
            DynamicPreviewGrid(
                withText = withText,
                withReply = withReply,
                modifier = Modifier,
                previews = previewAttachments
                    .map(VkAttachment::asUiPhoto)
                    .toImmutableList(),
                onClick = { index ->
                    currentOnClick(previewAttachments[index])
                },
                onLongClick = { index ->
                    currentOnLongClick(previewAttachments[index])
                }
            )
        }

        nonPreviewAttachments.forEach { attachment ->
            when (attachment.type) {
                AttachmentType.AUDIO -> {
                    Audio(
                        item = attachment as VkAudioDomain,
                        modifier = Modifier
                    )
                }

                AttachmentType.FILE -> {
                    File(
                        item = attachment as VkFileDomain,
                        modifier = Modifier,
                        onClick = { currentOnClick(attachment) },
                        onLongClick = { currentOnLongClick(attachment) }
                    )
                }

                AttachmentType.LINK -> {
                    Link(
                        item = attachment as VkLinkDomain,
                        modifier = Modifier,
                        onClick = { currentOnClick(attachment) },
                        onLongClick = { currentOnLongClick(attachment) }
                    )
                }

                AttachmentType.STICKER -> {
                    Sticker(
                        url = (attachment as VkStickerDomain).getUrl(
                            width = 256,
                            withBackground = false
                        )
                    )
                }

                AttachmentType.GIFT -> {
                    Gift(url = (attachment as VkGiftDomain).getDefaultThumbSizeOrLess())
                }

                AttachmentType.VIDEO_MESSAGE -> {
                    val videoMessage = attachment as VkVideoMessageDomain
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(2.dp)
                            .clickable { currentOnClick(attachment) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = videoMessage.image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(192.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.25f)),
                            contentScale = ContentScale.Crop
                        )

                        // Center play button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(dev.meloda.fast.ui.R.drawable.ic_play_arrow_fill_round_24),
                                contentDescription = "Play video note",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Duration badge
                        if (videoMessage.duration > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                val mins = videoMessage.duration / 60
                                val secs = videoMessage.duration % 60
                                Text(
                                    text = String.format(java.util.Locale.getDefault(), "%d:%02d", mins, secs),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                AttachmentType.AUDIO_MESSAGE -> {
                    fun downsampleWaveform(wave: List<Int>): List<Int> {
                        val result = mutableListOf<Int>()
                        for (i in wave.indices step 2) {
                            val first = wave[i]
                            val second = wave.getOrNull(i + 1) ?: first
                            result.add((first + second) / 2)
                        }
                        return result
                    }

                    fun amplifyWaveform(wave: List<Int>, originalMax: Int): List<Int> {
                        val newMax = wave.maxOrNull() ?: 1
                        val factor = if (newMax == 0) 1.0 else originalMax.toDouble() / newMax
                        return wave.map { (it * factor).toInt() }
                    }

                    val audioMessage = attachment as VkAudioMessageDomain
                    val audioKey = audioMessage.linkMp3.ifBlank { audioMessage.linkOgg }
                    val isCurrentAudio = voicePlayback.key == audioKey
                    val progress = if (isCurrentAudio) voicePlayback.progress else 0f

                    val displayedWaveform = if (audioMessage.waveform.isNotEmpty()) {
                        val downsampled = if (audioMessage.waveform.size > 50) {
                            downsampleWaveform(audioMessage.waveform)
                        } else {
                            audioMessage.waveform
                        }
                        amplifyWaveform(downsampled, audioMessage.waveform.maxOrNull() ?: 1)
                    } else {
                        emptyList()
                    }

                    val playedCount = (displayedWaveform.size * progress).toInt()

                    AudioMessage(
                        waveform = displayedWaveform
                            .mapIndexed { index, value ->
                                WaveForm(value = value, played = index < playedCount)
                            }
                            .toImmutableList(),
                        isPlaying = isCurrentAudio && voicePlayback.isPlaying,
                        durationSec = audioMessage.duration,
                        currentProgress = progress,
                        onPlayClick = { currentOnClick(attachment) }
                    )
                }

                else -> {
                    Text(
                        text = buildAnnotatedString {
                            append("Unsupported attachment: [${attachment.type}]")
                            addStyle(SpanStyle(fontWeight = FontWeight.Medium), 0, length)
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), 0, length)
                            addStyle(
                                SpanStyle(textDecoration = TextDecoration.Underline),
                                0,
                                length
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

fun VkAttachment.asUiPhoto(): UiPreview {
    return when (this) {
        is VkPhotoDomain -> {
            val size = this.getDefault()!!
            UiPreview(
                id = this.id,
                url = size.url,
                width = size.width,
                height = size.height,
                isVideo = false
            )
        }

        is VkVideoDomain -> {
            val size = this.getDefault() ?: VkVideoDomain.VideoImage(
                width = 1280,
                height = 720,
                url = "",
                withPadding = false
            )

            UiPreview(
                id = this.id,
                url = size.url,
                width = size.width,
                height = size.height,
                isVideo = true,
                durationSec = this.duration
            )
        }

        is VkFileDomain -> {
            when {
                this.preview?.video != null -> {
                    val video = this.preview?.video!!

                    UiPreview(
                        id = id,
                        url = video.src,
                        width = video.width,
                        height = video.height,
                        isVideo = true
                    )
                }

                this.preview?.photo != null -> {
                    val photoSize = this.preview?.photo?.sizes?.first()!!

                    UiPreview(
                        id = id,
                        url = photoSize.src,
                        width = photoSize.width,
                        height = photoSize.height,
                        isVideo = false
                    )
                }

                else -> error("Unsupported type: $this")
            }
        }

        else -> error("Unsupported type: $this")
    }
}

@Immutable
data class UiPreview(
    val id: Long,
    val url: String,
    val width: Int,
    val height: Int,
    val isVideo: Boolean,
    val durationSec: Int = 0
)
