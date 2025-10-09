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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList

private val previewTypes = listOf(
    AttachmentType.PHOTO,
    AttachmentType.VIDEO
)

@Composable
fun Attachments(
    modifier: Modifier = Modifier,
    attachments: ImmutableList<out VkAttachment>,
    onClick: (VkAttachment) -> Unit = {},
    onLongClick: (VkAttachment) -> Unit = {}
) {
    if (attachments.isEmpty()) return

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    Column(modifier = modifier) {
        val previewAttachments by remember(attachments) {
            derivedStateOf {
                attachments.values.filter { it.type in previewTypes }
            }
        }

        val nonPreviewAttachments by remember(attachments) {
            derivedStateOf {
                attachments.values.filterNot { it.type in previewTypes }
                    .sortedBy { it.type.ordinal }
            }
        }

        if (previewAttachments.isNotEmpty()) {
            Previews(
                modifier = Modifier,
                photos = previewAttachments
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
                        item = attachment as VkStickerDomain
                    )
                }

                AttachmentType.GIFT -> {
                    Gift(item = attachment as VkGiftDomain)
                }

                AttachmentType.VIDEO_MESSAGE -> {
                    var isPlaying by remember {
                        mutableStateOf(false)
                    }

                    val imageSize by animateDpAsState(
                        targetValue = if (isPlaying) 256.dp else 192.dp,
                        label = "video message preview animation",
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(1.dp)
                    ) {
                        AsyncImage(
                            model = (attachment as VkVideoMessageDomain).image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(imageSize)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .clickable {
                                    isPlaying = !isPlaying
                                },
                            contentScale = ContentScale.Crop
                        )
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
                    AudioMessage(
                        waveform = audioMessage.waveform
                            .let(::downsampleWaveform)
                            .let(::downsampleWaveform)
                            .let { amplifyWaveform(it, audioMessage.waveform.max()) }
                            .map(::WaveForm),
                        isPlaying = false,
                        onPlayClick = {}
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
                isVideo = true
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
    val isVideo: Boolean
)
