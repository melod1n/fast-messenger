package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType
import com.meloda.app.fast.model.api.data.VkFileData
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkFileDomain(
    val id: Int,
    val ownerId: Int,
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
