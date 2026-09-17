package dev.meloda.fast.model.api.domain

import android.os.Parcelable
import dev.meloda.fast.model.api.data.AttachmentType
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkVideoMessageDomain(
    val id: Long,
    val ownerId: Long?,
    val accessKey: String?,
    val duration: Int,
    val image: String?,
    val link: String?
) : VkAttachment, Parcelable {

    override val type: AttachmentType = AttachmentType.VIDEO_MESSAGE
}
