package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAudioDomain
import dev.meloda.fast.model.api.domain.VkFileDomain
import dev.meloda.fast.model.api.domain.VkLinkDomain
import dev.meloda.fast.model.api.domain.VkPhotoDomain
import dev.meloda.fast.model.api.domain.VkVideoDomain
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
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    Column(modifier = modifier) {
        if (attachments.isEmpty()) return

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

                else -> Unit
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
