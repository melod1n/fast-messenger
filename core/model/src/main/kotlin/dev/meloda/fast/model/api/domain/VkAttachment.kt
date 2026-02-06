package dev.meloda.fast.model.api.domain

import androidx.compose.runtime.Immutable
import dev.meloda.fast.model.api.data.AttachmentType

@Immutable
interface VkAttachment {
    val type: AttachmentType
}
