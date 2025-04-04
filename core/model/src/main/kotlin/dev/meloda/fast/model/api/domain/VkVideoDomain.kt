package dev.meloda.fast.model.api.domain

import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.data.VkVideoData

@JsonClass(generateAdapter = true)
data class VkVideoDomain(
    val id: Long,
    val ownerId: Long,
    val images: List<VideoImage>,
    val firstFrames: List<VkVideoData.FirstFrame>?,
    val accessKey: String?,
    val title: String,
    val views: Int,
    val duration: Int
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.VIDEO

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

    sealed class ShapeKind(val value: Int) {
        data object Square : ShapeKind(0)
        data object Vertical : ShapeKind(1)
        data object Horizontal : ShapeKind(2)

        companion object {


            fun parse(value: Int) = when (value) {
                0 -> Square
                1 -> Vertical
                2 -> Horizontal
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }
    }

    override fun toString(): String {
        val result = StringBuilder(type.value).append(ownerId).append('_').append(id)
        if (!accessKey.isNullOrBlank()) {
            result.append('_')
            result.append(accessKey)
        }
        return result.toString()
    }
}
