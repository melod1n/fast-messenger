package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.api.model.data.VkFileData
import com.meloda.fast.model.base.UiText
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
) : VkMultipleAttachment {

    override val type: AttachmentType = AttachmentType.FILE

    val className: String = this::class.java.name

    override fun getUiText(size: Int): UiText =
        UiText.QuantityResource(R.plurals.attachment_files, size)

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )
}
