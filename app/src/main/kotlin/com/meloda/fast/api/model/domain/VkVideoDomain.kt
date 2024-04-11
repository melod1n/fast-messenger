package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.api.model.data.VkVideoData
import com.meloda.fast.model.base.UiText
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkVideoDomain(
    val id: Int,
    val ownerId: Int,
    val images: List<VideoImage>,
    val firstFrames: List<VkVideoData.FirstFrame>?,
    val accessKey: String?,
    val title: String,
) : VkMultipleAttachment {

    override val type: AttachmentType = AttachmentType.VIDEO

    val className: String = this::class.java.name

    override fun getUiText(size: Int): UiText =
        UiText.QuantityResource(R.plurals.attachment_videos, size)

    fun imageForWidth(width: Int): VideoImage? {
        return images.find { it.width == width }
    }

    fun imageForWidthAtLeast(width: Int): VideoImage? {
        var certainImages = images.sortedByDescending { it.width }
        var containsVertical = false
        for (image in images) {
            if (image.shapeKind == ShapeKind.Vertical) {
                containsVertical = true
                break
            }
        }

        if (containsVertical) {
            certainImages = certainImages.filter { it.shapeKind == ShapeKind.Vertical }
        }

        certainImages = certainImages.filter { it.width >= width }

        return certainImages.firstOrNull()
    }

    @JsonClass(generateAdapter = true)
    data class VideoImage(
        val width: Int,
        val height: Int,
        val url: String,
        val withPadding: Boolean,
    ) {

        var shapeKind: ShapeKind? = null

        init {
            val ratio = width.toFloat() / height.toFloat()

            shapeKind = when {
                ratio > 1 -> ShapeKind.Horizontal
                ratio < 1 -> ShapeKind.Vertical
                else -> ShapeKind.Square
            }
        }
    }

    open class ShapeKind(val value: Int) {
        object Square : ShapeKind(0)
        object Vertical : ShapeKind(1)
        object Horizontal : ShapeKind(2)

        companion object {


            fun parse(value: Int) = when (value) {
                0 -> Square
                1 -> Vertical
                2 -> Horizontal
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }
    }

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )

}
