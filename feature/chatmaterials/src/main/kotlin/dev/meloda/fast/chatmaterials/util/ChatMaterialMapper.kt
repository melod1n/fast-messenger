package dev.meloda.fast.chatmaterials.util

import android.util.Log
import dev.meloda.fast.chatmaterials.model.UiChatMaterial
import dev.meloda.fast.common.util.AndroidUtils
import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkAudioDomain
import dev.meloda.fast.model.api.domain.VkFileDomain
import dev.meloda.fast.model.api.domain.VkLinkDomain
import dev.meloda.fast.model.api.domain.VkPhotoDomain
import dev.meloda.fast.model.api.domain.VkVideoDomain
import java.util.Locale

fun VkAttachmentHistoryMessage.asPresentation(): UiChatMaterial? =
    when (val type = this.attachment.type) {
        AttachmentType.PHOTO -> {
            val attachment = this.attachment as VkPhotoDomain
            UiChatMaterial.Photo(
                cmId = this.cmId,
                previewUrl = attachment.getSizeOrSmaller(VkPhotoDomain.SIZE_TYPE_1080_1024)?.url.orEmpty()
            )
        }

        AttachmentType.VIDEO -> {
            val attachment = this.attachment as VkVideoDomain

            val duration = attachment.duration

            val days = duration / (24 * 3600)
            val hours = (duration % (24 * 3600)) / 3600
            val minutes = (duration % 3600) / 60
            val seconds = duration % 60

            val args = mutableListOf<Int>()
            if (days > 0) args.add(days)
            if (hours > 0) args.add(hours)
            args.add(minutes)
            args.add(seconds)

            val builder = StringBuilder()
            if (days > 0) builder.append("%02d:")
            if (hours > 0) builder.append("%02d:")
            builder.append("%02d:%02d")

            val formattedDuration =
                builder.toString().format(Locale.getDefault(), *args.toTypedArray())

            UiChatMaterial.Video(
                cmId = this.cmId,
                previewUrl = attachment.images.maxByOrNull(VkVideoDomain.VideoImage::width)?.url.orEmpty(),
                title = attachment.title,
                views = attachment.views,
                duration = formattedDuration
            )
        }

        AttachmentType.AUDIO -> {
            val attachment = this.attachment as VkAudioDomain

            val duration = attachment.duration

            val days = duration / (24 * 3600)
            val hours = (duration % (24 * 3600)) / 3600
            val minutes = (duration % 3600) / 60
            val seconds = duration % 60

            val args = mutableListOf<Int>()
            if (days > 0) args.add(days)
            if (hours > 0) args.add(hours)
            args.add(minutes)
            args.add(seconds)

            val builder = StringBuilder()
            if (days > 0) builder.append("%02d:")
            if (hours > 0) builder.append("%02d:")
            builder.append("%d:%02d")

            val formattedDuration =
                builder.toString().format(Locale.getDefault(), *args.toTypedArray())

            UiChatMaterial.Audio(
                cmId = this.cmId,
                previewUrl = null,
                title = attachment.title,
                artist = attachment.artist,
                duration = formattedDuration
            )
        }

        AttachmentType.FILE -> {
            val attachment = this.attachment as VkFileDomain

            val previewUrl: String? = when (val preview = attachment.preview) {
                null -> null

                else -> {
                    when {
                        preview.photo != null -> {
                            val size = preview.photo?.sizes?.maxByOrNull { it.width }
                            size?.src
                        }

                        preview.video != null -> {
                            val size = preview.video?.src
                            size
                        }

                        else -> null
                    }
                }
            }

            UiChatMaterial.File(
                cmId = this.cmId,
                title = attachment.title,
                previewUrl = previewUrl,
                size = AndroidUtils.bytesToHumanReadableSize(attachment.size.toDouble()),
                extension = attachment.ext.take(4)
            )
        }

        AttachmentType.LINK -> {
            val attachment = this.attachment as VkLinkDomain

            UiChatMaterial.Link(
                cmId = this.cmId,
                title = attachment.title,
                previewUrl = attachment.photo?.getMaxSize()?.url,
                url = attachment.url,
                urlFirstChar = attachment.url.replaceFirst("http://", "")
                    .replaceFirst("https://", "")
                    .take(1)
                    .uppercase()
            )
        }

        else -> {
            Log.w("ChatMaterialMapper", "Unsupported type: $type")
            null
        }
    }
