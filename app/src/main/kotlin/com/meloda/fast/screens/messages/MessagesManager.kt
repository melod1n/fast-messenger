package com.meloda.fast.screens.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isNotEmpty
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkPhoto
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.widget.BoundedFrameLayout
import com.meloda.fast.widget.BoundedLinearLayout
import kotlin.math.roundToInt

object MessagesManager {

    fun setRootMaxWidth(
        layout: View
    ) {
        val maxWidth = (AppGlobal.screenWidth * 0.7).roundToInt()

        if (layout is BoundedFrameLayout) {
            layout.maxWidth = maxWidth
        } else if (layout is BoundedLinearLayout) {
            layout.maxWidth = maxWidth
        }
    }

    fun loadPhotos(
        context: Context,
        message: VkMessage,
        photosContainer: LinearLayoutCompat
    ) {
        photosContainer.removeAllViews()

        message.attachments?.let { attachments ->
            val photos = attachments.map { it as VkPhoto }

            photos.forEach { photo ->
                val size = photo.sizeOfType('m') ?: return

                val newPhoto = ShapeableImageView(context).also {
                    it.layoutParams = LinearLayoutCompat.LayoutParams(
                        AndroidUtils.px(size.width).roundToInt(),
                        AndroidUtils.px(size.height).roundToInt()
                    )
                    it.shapeAppearanceModel =
                        it.shapeAppearanceModel.withCornerSize { AndroidUtils.px(4) }
                    it.scaleType = ImageView.ScaleType.CENTER_CROP
                }

                val spacer = Space(context).also {
                    it.layoutParams = LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        AndroidUtils.px(5).roundToInt()
                    )
                }

                if (photosContainer.isNotEmpty())
                    photosContainer.addView(spacer)

                photosContainer.addView(newPhoto)

                newPhoto.load(size.url)
            }
        }
    }

    fun loadMessageAvatar(
        message: VkMessage,
        messageUser: VkUser?,
        messageGroup: VkGroup?,
        imageView: ImageView
    ) {
        val avatar = when {
            message.isUser() && messageUser != null && !messageUser.photo200.isNullOrBlank() -> messageUser.photo200
            message.isGroup() && messageGroup != null && !messageGroup.photo200.isNullOrBlank() -> messageGroup.photo200
            else -> null
        }

        imageView.load(avatar) { crossfade(100) }
    }

    fun setMessageText(
        message: VkMessage,
        textView: TextView
    ) {
        textView.text = message.text ?: "[no_message]"
    }


}