package com.meloda.fast.screens.messages

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Space
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.*
import com.meloda.fast.databinding.*
import com.meloda.fast.extensions.*
import com.meloda.fast.extensions.ImageLoader.clear
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.widget.RoundedFrameLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// TODO: 9/29/2021 use recyclerview for viewing attachments
class AttachmentInflater constructor(
    private val context: Context,
    private val container: LinearLayoutCompat,
    private val textContainer: LinearLayoutCompat,
    private val message: VkMessage,
    private val profiles: Map<Int, VkUser>,
    private val groups: Map<Int, VkGroup>
) {
    private lateinit var attachments: List<VkAttachment>

    private val inflater = LayoutInflater.from(context)

    private val playColor = ContextCompat.getColor(context, R.color.a3_700)
    private val playBackgroundColor = ContextCompat.getColor(context, R.color.a3_200)

    var photoClickListener: ((url: String) -> Unit)? = null

    fun setPhotoClickListener(unit: ((url: String) -> Unit)?): AttachmentInflater {
        this.photoClickListener = unit
        return this
    }

    fun inflate() {
        container.removeAllViews()

        if (textContainer.childCount > 1) {
            textContainer.removeViews(1, textContainer.childCount - 1)
        }

        if (message.attachments.isNullOrEmpty()) return
        attachments = message.attachments!!

        if (attachments.size == 1) {
            when (val attachment = attachments[0]) {
                is VkSticker -> return sticker(attachment)
                is VkWall -> return wall(attachment)
                is VkVoiceMessage -> return voice(attachment)
                is VkCall -> return call(attachment)
                is VkGraffiti -> return graffiti(attachment)
                is VkGift -> return gift(attachment)
                is VkStory -> return story(attachment)
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

                else -> Log.e(
                    "Attachment inflater",
                    "Unknown attachment type: ${attachment.javaClass.name}"
                )
            }
        }

    }

    private fun photo(photo: VkPhoto) {
        val size = photo.getSizeOrSmaller(VkPhoto.SIZE_TYPE_807) ?: return

        val colorBackground = ContextCompat.getColor(
            context,
            R.color.colorBackground
        )
        val colorSecondary = ContextCompat.getColor(
            context,
            R.color.colorSecondary
        )

        val constraintLayout = ConstraintLayout(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val cornersRadius = 8.dpToPx().toFloat()
        val photoMargin = 1F.dpToPx()

        val border = ImageView(context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }

            loadWithGlide(
                drawable = ColorDrawable(colorSecondary),
                transformations = listOf(TypeTransformations.RoundedCornerCrop(cornersRadius.toInt())),
                priority = Priority.IMMEDIATE,
                cacheStrategy = DiskCacheStrategy.NONE
            )
        }
        constraintLayout.addView(border)

        val newPhoto = ShapeableImageView(context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                120.dpToPx(),
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

                dimensionRatio = "1:1.75"
                updateMarginsRelative(
                    start = photoMargin,
                    top = photoMargin,
                    end = photoMargin,
                    bottom = photoMargin
                )
            }

            adjustViewBounds = true

            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(7.5F.dpToPx().toFloat())

            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        constraintLayout.addView(newPhoto)

        if (photoClickListener != null) {
            newPhoto.setOnClickListener { photoClickListener?.invoke(size.url) }
        } else {
            newPhoto.setOnClickListener(null)
        }

        val spacer = Space(context).apply {
            layoutParams =
                LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5.dpToPx())
        }

        if (container.isNotEmpty()) {
            container.addView(spacer)
        }

        if (attachments.size == 1) {
            val roundedLayout = RoundedFrameLayout(context).apply {
                setTopRightCornerRadius((if (message.isOut) 30 else 40).toFloat())
                setTopLeftCornerRadius((if (message.isOut) 40 else 30).toFloat())
                setBottomRightCornerRadius((if (message.isOut) 5 else 40).toFloat())
                setBottomLeftCornerRadius((if (message.isOut) 40 else 5).toFloat())
            }

            roundedLayout.addView(constraintLayout)
            container.addView(roundedLayout)
        } else {
            container.addView(constraintLayout)
        }

        newPhoto.loadWithGlide(
            url = size.url, crossFade = true,
            placeholderDrawable = ColorDrawable(colorBackground)
        )
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
            layoutParams = FrameLayout.LayoutParams(size.width.dpToPx(), size.height.dpToPx())

            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(5.dpToPx().toFloat())
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val play = AppCompatImageView(context).apply {
            val playSize = 50.dpToPx()

            layoutParams = FrameLayout.LayoutParams(playSize, playSize).apply {
                gravity = Gravity.CENTER
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
            layoutParams =
                LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5.dpToPx())
        }

        if (container.isNotEmpty())
            container.addView(spacer)

        container.addView(layout)

        newPhoto.loadWithGlide(url = size.url, crossFade = true)
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
        val binding = ItemMessageAttachmentLinkBinding.inflate(inflater, textContainer, true)

        binding.title.text = link.title
        binding.title.toggleVisibility(!link.title.isNullOrBlank())

        binding.caption.text = link.caption
        binding.caption.toggleVisibility(!link.caption.isNullOrBlank())

        link.photo?.getSizeOrSmaller('y')?.let { size ->
            binding.preview.loadWithGlide(url = size.url, crossFade = true)
            binding.linkIcon.gone()
            return
        }

        binding.preview.setImageDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.a3_200
                )
            )
        )
        binding.linkIcon.visible()
    }

    private fun sticker(sticker: VkSticker) {
        val binding = ItemMessageAttachmentStickerBinding.inflate(inflater, container, true)

        val url = sticker.urlForSize(352)

        binding.image.run {
            val size = 140.dpToPx()

            layoutParams = LinearLayoutCompat.LayoutParams(size, size)

            loadWithGlide(url = url, crossFade = true)
        }
    }

    private fun wall(wall: VkWall) {
        val binding = ItemMessageAttachmentWallPostBinding.inflate(inflater, textContainer, true)

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
        binding.postTitle.gone()

        binding.avatar.toggleVisibility(group != null || user != null)

        if (binding.avatar.isVisible) {
            binding.avatar.loadWithGlide(url = avatar, crossFade = true)
        } else {
            binding.avatar.clear()
        }

        binding.title.text = title

        binding.date.text = SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(wall.date * 1000L)
    }

    private fun voice(voiceMessage: VkVoiceMessage) {
        val binding = ItemMessageAttachmentVoiceBinding.inflate(inflater, textContainer, true)

        if (message.isOut) {
            val padding = 6.dpToPx()
            binding.root.updatePadding(
                bottom = padding,
                left = padding
            )
        }
        val waveform = IntArray(voiceMessage.waveform.size)
        voiceMessage.waveform.forEachIndexed { index, i -> waveform[index] = i }

        binding.waveform.sample = waveform
        binding.waveform.maxProgress = 100f
        binding.waveform.progress = 100f

        binding.duration.text = SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        ).format(voiceMessage.duration * 1000L)
    }

    private fun call(call: VkCall) {
        val binding = ItemMessageAttachmentCallBinding.inflate(inflater, textContainer, true)

        if (message.isOut)
            binding.root.updatePadding(
                bottom = 5.dpToPx(),
                left = 6.dpToPx()
            )

        val callType =
            context.getString(
                if (call.initiatorId == UserConfig.userId) R.string.message_call_type_outgoing
                else R.string.message_call_type_incoming
            )

        binding.type.text = callType

        var callState =
            context.getString(
                if (call.state == "reached") R.string.message_call_state_ended
                else if (call.state == "canceled_by_initiator") {
                    if (call.initiatorId == UserConfig.userId) R.string.message_call_state_cancelled
                    else R.string.message_call_state_missed
                } else R.string.message_call_unknown
            )

        if (callState == context.getString(R.string.message_call_unknown)) callState = call.state

        binding.state.text = callState
    }

    private fun graffiti(graffiti: VkGraffiti) {
        val binding = ItemMessageAttachmentGraffitiBinding.inflate(inflater, container, true)

        val url = graffiti.url

        val size = 140.dpToPx()

        val heightCoefficient = graffiti.height / size.toFloat()

        binding.image.run {
            layoutParams = LinearLayoutCompat.LayoutParams(
                size,
                (graffiti.height / heightCoefficient).roundToInt()
            )

            loadWithGlide(url = url, crossFade = true)
        }
    }

    private fun gift(gift: VkGift) {
        val binding = ItemMessageAttachmentGiftBinding.inflate(inflater, container, true)

        val url = gift.thumb256 ?: gift.thumb96 ?: gift.thumb48

        binding.image.run {
            val size = 140.dpToPx()

            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(12.dpToPx().toFloat())

            layoutParams = LinearLayoutCompat.LayoutParams(size, size)

            loadWithGlide(url = url, crossFade = true)
        }
    }

    private fun story(story: VkStory) {
        val binding = ItemMessageAttachmentStoryBinding.inflate(inflater, container, true)

        val photoUrl = story.photo?.getSizeOrSmaller(VkPhoto.SIZE_TYPE_807)?.url

        val dimmerDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_message_attachment_story_image_dimmer)

        val cornersRadius = 24.dpToPx()

        binding.caption.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val margin = cornersRadius / 2
            updateMarginsRelative(
                top = margin,
                start = margin,
                end = margin,
                bottom = margin
            )
        }

        binding.dimmer.loadWithGlide(
            drawable = dimmerDrawable,
            transformations = listOf(TypeTransformations.RoundedCornerCrop(cornersRadius)),
            priority = Priority.IMMEDIATE,
            cacheStrategy = DiskCacheStrategy.NONE
        )

        binding.image.run {
            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(cornersRadius.toFloat())

            loadWithGlide(
                url = photoUrl,
                crossFade = true,
                placeholderDrawable = ColorDrawable(Color.GRAY)
            )
        }

        if (story.ownerId == UserConfig.userId) {
            binding.caption.text = context.getString(R.string.message_attachment_story_your_story)
        } else {
            val storyOwnerUser = if (story.isFromUser()) profiles[story.ownerId] else null
            val storyOwnerGroup = if (story.isFromGroup()) groups[story.ownerId] else null

            val ownerName = when {
                storyOwnerUser != null -> storyOwnerUser.fullName
                storyOwnerGroup != null -> storyOwnerGroup.name
                else -> null
            }

            binding.caption.text = context.getString(
                R.string.message_attachment_story_story_from,
                ownerName
            )
            binding.caption.toggleVisibility(ownerName != null)
            binding.dimmer.toggleVisibility(binding.caption.isVisible)
        }
    }
}