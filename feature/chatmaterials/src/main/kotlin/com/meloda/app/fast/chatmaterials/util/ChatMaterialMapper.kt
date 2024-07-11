package com.meloda.app.fast.chatmaterials.util

import com.meloda.app.fast.chatmaterials.model.UiChatMaterial
import com.meloda.app.fast.model.api.data.AttachmentType
import com.meloda.app.fast.model.api.domain.VkAttachmentHistoryMessage
import com.meloda.app.fast.model.api.domain.VkAudioDomain
import com.meloda.app.fast.model.api.domain.VkFileDomain
import com.meloda.app.fast.model.api.domain.VkLinkDomain
import com.meloda.app.fast.model.api.domain.VkPhotoDomain
import com.meloda.app.fast.model.api.domain.VkVideoDomain
import java.text.SimpleDateFormat
import java.util.Locale

fun VkAttachmentHistoryMessage.asPresentation(): UiChatMaterial =
    when (val type = this.attachment.type) {
        AttachmentType.PHOTO -> {
            val attachment = this.attachment as VkPhotoDomain
            UiChatMaterial.Photo(
                previewUrl = attachment.getSizeOrSmaller(VkPhotoDomain.SIZE_TYPE_1080_1024)?.url.orEmpty()
            )
        }

        AttachmentType.VIDEO -> {
            val attachment = this.attachment as VkVideoDomain
            UiChatMaterial.Video(
                previewUrl = attachment.images.firstOrNull()?.url.orEmpty()
            )
        }

        AttachmentType.AUDIO -> {
            val attachment = this.attachment as VkAudioDomain
            UiChatMaterial.Audio(
                previewUrl = null,
                title = attachment.title,
                artist = attachment.artist,
                duration = SimpleDateFormat(
                    "mm:ss",
                    Locale.getDefault()
                ).format(attachment.duration)
            )
        }

        AttachmentType.FILE -> {
            val attachment = this.attachment as VkFileDomain
            UiChatMaterial.File(
                title = attachment.title
            )
        }

        AttachmentType.LINK -> {
            val attachment = this.attachment as VkLinkDomain
            UiChatMaterial.Link(
                title = attachment.title ?: attachment.url,
                previewUrl = attachment.photo?.getMaxSize()?.url
            )
        }

        else -> throw IllegalArgumentException("Unsupported type: $type")
    }
