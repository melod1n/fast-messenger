package com.meloda.fast.screens.messages

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Space
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.*
import com.meloda.fast.databinding.*
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.widget.RoundedFrameLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class AttachmentInflater constructor(
    private val context: Context,
    private val container: LinearLayoutCompat,
    private val message: VkMessage,
    private val profiles: Map<Int, VkUser>,
    private val groups: Map<Int, VkGroup>
) {
    private lateinit var attachments: List<VkAttachment>

    private val inflater = LayoutInflater.from(context)

    private val playColor = ContextCompat.getColor(context, R.color.a3_700)
    private val playBackgroundColor = ContextCompat.getColor(context, R.color.a3_200)

    fun inflate() {
        if (message.attachments.isNullOrEmpty()) return
        attachments = message.attachments!!

        container.removeAllViews()

        if (attachments.size == 1) {
            when (val attachment = attachments[0]) {
                is VkSticker -> return sticker(attachment)
                is VkWall -> return wall(attachment)
            }
        }

        if (attachments.size > 1) {
            if (VkUtils.isAttachmentsHaveOneType(attachments) && attachments[0] is VkPhoto) {
                return attachments.forEach { photo(it as VkPhoto) }
            }

            if (VkUtils.isAttachmentsHaveOneType(attachments) && attachments[0] is VkVideo) {
                return attachments.forEach { video(it as VkVideo) }
            }

            if (VkUtils.isAttachmentsHaveOneType(attachments) && attachments[0] is VkAudio) {
                return attachments.forEach { audio(it as VkAudio) }
            }

            if (VkUtils.isAttachmentsHaveOneType(attachments) && attachments[0] is VkFile) {
                return attachments.forEach { file(it as VkFile) }
            }
        }

        attachments.forEach { attachment ->
            when (attachment) {
                is VkPhoto -> photo(attachment)
                is VkVideo -> video(attachment)
                is VkAudio -> audio(attachment)
                is VkFile -> file(attachment)
                is VkLink -> link(attachment)
                is VkStory -> story(attachment)

                else -> Log.e(
                    "Attachment inflater",
                    "Unknown attachment type: ${attachment.javaClass.name}"
                )
            }
        }

    }

    private fun photo(photo: VkPhoto) {
        val size = photo.sizeOfType('m') ?: return

        val newPhoto = ShapeableImageView(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                AndroidUtils.px(size.width).roundToInt(),
                AndroidUtils.px(size.height).roundToInt()
            )

            shapeAppearanceModel =
                shapeAppearanceModel.withCornerSize {
                    AndroidUtils.px(5)
                }

            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val spacer = Space(context).also {
            it.layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                AndroidUtils.px(5).roundToInt()
            )
        }

        if (container.isNotEmpty())
            container.addView(spacer)

        if (attachments.size == 1) {
            val roundedLayout = RoundedFrameLayout(context).apply {
                setTopRightCornerRadius((if (message.isOut) 30 else 40).toFloat())
                setTopLeftCornerRadius((if (message.isOut) 40 else 30).toFloat())
                setBottomRightCornerRadius((if (message.isOut) 5 else 40).toFloat())
                setBottomLeftCornerRadius((if (message.isOut) 40 else 5).toFloat())
            }

            roundedLayout.addView(newPhoto)
            container.addView(roundedLayout)
        } else {
            container.addView(newPhoto)
        }

        newPhoto.load(size.url) { crossfade(100) }
    }

    private fun video(video: VkVideo) {
        val size = video.images[1]

        val layout = FrameLayout(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val newPhoto = ShapeableImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                AndroidUtils.px(size.width).roundToInt(),
                AndroidUtils.px(size.height).roundToInt()
            )

            shapeAppearanceModel =
                shapeAppearanceModel.withCornerSize { AndroidUtils.px(5) }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val play = AppCompatImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                AndroidUtils.px(50).roundToInt(),
                AndroidUtils.px(50).roundToInt()
            ).also {
                it.gravity = Gravity.CENTER
            }

            backgroundTintList = ColorStateList.valueOf(playBackgroundColor)
            imageTintList = ColorStateList.valueOf(playColor)

            setBackgroundResource(R.drawable.ic_play_button_circle_background)
            setImageResource(R.drawable.ic_round_play_arrow_24)

            setPadding(12)
        }

        layout.addView(newPhoto)
        layout.addView(play)

        val spacer = Space(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                AndroidUtils.px(5).roundToInt()
            )
        }

        if (container.isNotEmpty())
            container.addView(spacer)

        container.addView(layout)

        newPhoto.load(size.url) { crossfade(100) }
    }

    private fun audio(audio: VkAudio) {
        val binding = ItemMessageAttachmentAudioBinding.inflate(inflater, container, true)

        binding.title.text = audio.title
        binding.artist.text = "%s | %s".format(
            audio.artist,
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(audio.duration * 1000L)
        )
    }

    private fun file(file: VkFile) {
        val binding = ItemMessageAttachmentFileBinding.inflate(inflater, container, true)

        binding.title.text = file.title
        binding.size.text = "%s | %s".format(
            AndroidUtils.bytesToHumanReadableSize(file.size.toDouble()),
            file.ext.uppercase()
        )
    }

    private fun link(link: VkLink) {
        val binding = ItemMessageAttachmentLinkBinding.inflate(inflater, container, true)

        binding.title.text = link.title
        binding.title.isVisible = !link.title.isNullOrBlank()

        binding.caption.text = link.caption
        binding.caption.isVisible = !link.caption.isNullOrBlank()

        binding.preview.shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(40f)
            .build()
            .let {
                binding.preview.shapeAppearanceModel = it
            }

        link.photo?.sizeOfType('m')?.let {
            binding.preview.load(it.url) { crossfade(150) }
            binding.preview.isVisible = true
            return
        }

        binding.preview.isVisible = false
    }

    private fun sticker(sticker: VkSticker) {
        val binding = ItemMessageAttachmentStickerBinding.inflate(inflater, container, true)

        val url = sticker.urlForSize(352)

        with(binding.image) {
            layoutParams = LinearLayoutCompat.LayoutParams(
                AndroidUtils.px(180).roundToInt(),
                AndroidUtils.px(180).roundToInt()
            )

            load(url) { crossfade(150) }
        }
    }

    private fun wall(wall: VkWall) {
        val binding = ItemMessageAttachmentWallPostBinding.inflate(inflater, container, true)

        val group = if (wall.fromId > 0) null else groups[wall.fromId]
        val user = if (wall.fromId < 0) null else profiles[wall.fromId]

        val postTitleRes = when {
            group != null && user == null -> R.string.post_type_community
            user != null && group == null -> R.string.post_type_user
            else -> R.string.post_type_unknown
        }

        val avatar = when {
            group == null && user != null -> user.photo200
            user == null && group != null -> group.photo200
            else -> null
        }

        val title = when {
            group == null && user != null -> user.fullName
            user == null && group != null -> group.name
            else -> "..."
        }

        binding.postTitle.text = context.getString(postTitleRes)
        binding.postTitle.isVisible = false

        binding.avatar.isVisible = group != null || user != null
        binding.avatar.shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(40f)
            .build()
            .let {
                binding.avatar.shapeAppearanceModel = it
            }

        if (binding.avatar.isVisible) {
            binding.avatar.load(avatar) { crossfade(150) }
        } else {
            binding.avatar.setImageDrawable(null)
        }

        binding.title.text = title

        binding.date.text = SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(wall.date * 1000L)
    }

    private fun story(story: VkStory) {

    }

}