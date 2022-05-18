package com.meloda.fast.api.model.attachments

import android.os.Parcelable
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.base.attachments.BaseVkVideo
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkVideo(
    val id: Int,
    val ownerId: Int,
    val images: List<VideoImage>,
    val firstFrames: List<BaseVkVideo.FirstFrame>?,
    val accessKey: String?,
    val title: String
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name

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

    @Parcelize
    data class VideoImage(
        val width: Int,
        val height: Int,
        val url: String,
        val withPadding: Boolean
    ) : Parcelable {

        @IgnoredOnParcel
        var shapeKind: ShapeKind

        init {
            val ratio = width.toFloat() / height.toFloat()

            shapeKind = when {
                ratio > 1 -> ShapeKind.Horizontal
                ratio < 1 -> ShapeKind.Vertical
                else -> ShapeKind.Square
            }
        }
    }

    sealed class ShapeKind {
        object Vertical : ShapeKind()
        object Horizontal : ShapeKind()
        object Square : ShapeKind()
    }

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )

}