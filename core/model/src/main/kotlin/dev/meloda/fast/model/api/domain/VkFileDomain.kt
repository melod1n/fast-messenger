package dev.meloda.fast.model.api.domain

import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.data.VkFileData

@JsonClass(generateAdapter = true)
data class VkFileDomain(
    val id: Long,
    val ownerId: Long,
    val title: String,
    val ext: String,
    val size: Int,
    val url: String,
    val accessKey: String?,
    val preview: VkFileData.Preview?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.FILE

    override fun toString(): String {
        val result = StringBuilder(type.value).append(ownerId).append('_').append(id)
        if (!accessKey.isNullOrBlank()) {
            result.append('_')
            result.append(accessKey)
        }
        return result.toString()
    }
}
