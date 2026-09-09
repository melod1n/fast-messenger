package dev.meloda.fast.model.api.domain

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.data.VkVideoData
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class VkVideoDomain(
    val id: Long,
    val ownerId: Long,
    val images: List<VideoImage>,
    val firstFrames: List<VkVideoData.FirstFrame>?,
    val accessKey: String?,
    val title: String,
    val views: Int,
    val duration: Int,
    val isShortVideo: Boolean,
    val player: String? = null,
    val directUrl: String? = null
) : VkAttachment, Parcelable {

    override val type: AttachmentType = AttachmentType.VIDEO

    fun imageForWidth(width: Int): VideoImage? {
        return images.find { it.width == width }
    }

    fun getDefault(): VideoImage? {
        return imageForWidthAtLeast(720)
            ?: images.maxByOrNull { it.width }
            ?: firstFrames?.firstOrNull()?.let {
                VideoImage(
                    width = it.width,
                    height = it.height,
                    url = it.url,
                    withPadding = false
                )
            }
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

        val filteredCertainImages = certainImages.filter { it.width >= width }

        return filteredCertainImages
            .ifEmpty { certainImages }
            .firstOrNull()
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class VideoImage(
        val width: Int,
        val height: Int,
        val url: String,
        val withPadding: Boolean,
    ) : Parcelable {

        var shapeKind: ShapeKind? = null

        init {
            val ratio = if (height > 0) width.toFloat() / height.toFloat() else 1f

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
        }
        if (!accessKey.isNullOrBlank()) {
            result.append(accessKey)
        }
        return result.toString()
    }
}
