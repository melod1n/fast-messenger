package com.meloda.fast.screens.messages

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Space
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMarginsRelative
import androidx.core.view.updatePadding
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.attachments.VkAudio
import com.meloda.fast.api.model.attachments.VkCall
import com.meloda.fast.api.model.attachments.VkFile
import com.meloda.fast.api.model.attachments.VkGift
import com.meloda.fast.api.model.attachments.VkGraffiti
import com.meloda.fast.api.model.attachments.VkLink
import com.meloda.fast.api.model.attachments.VkPhoto
import com.meloda.fast.api.model.attachments.VkSticker
import com.meloda.fast.api.model.attachments.VkStory
import com.meloda.fast.api.model.attachments.VkVideo
import com.meloda.fast.api.model.attachments.VkVoiceMessage
import com.meloda.fast.api.model.attachments.VkWall
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.databinding.ItemMessageAttachmentAudioBinding
import com.meloda.fast.databinding.ItemMessageAttachmentCallBinding
import com.meloda.fast.databinding.ItemMessageAttachmentFileBinding
import com.meloda.fast.databinding.ItemMessageAttachmentForwardsBinding
import com.meloda.fast.databinding.ItemMessageAttachmentGeoBinding
import com.meloda.fast.databinding.ItemMessageAttachmentGiftBinding
import com.meloda.fast.databinding.ItemMessageAttachmentGraffitiBinding
import com.meloda.fast.databinding.ItemMessageAttachmentLinkBinding
import com.meloda.fast.databinding.ItemMessageAttachmentPhotoBinding
import com.meloda.fast.databinding.ItemMessageAttachmentReplyBinding
import com.meloda.fast.databinding.ItemMessageAttachmentStickerBinding
import com.meloda.fast.databinding.ItemMessageAttachmentStoryBinding
import com.meloda.fast.databinding.ItemMessageAttachmentVideoBinding
import com.meloda.fast.databinding.ItemMessageAttachmentVoiceBinding
import com.meloda.fast.databinding.ItemMessageAttachmentWallPostBinding
import com.meloda.fast.ext.ImageLoader.clear
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.TypeTransformations
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.orDots
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.toggleVisibilityIfHasContent
import com.meloda.fast.ext.visible
import com.meloda.fast.model.base.parseString
import com.meloda.fast.util.AndroidUtils
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class AttachmentInflater constructor(
    private val context: Context,
    private val container: LinearLayoutCompat,
    private val replyContainer: FrameLayout,
    private val timeReadContainer: View,
    private val message: VkMessage,
    private val profiles: Map<Int, VkUser>,
    private val groups: Map<Int, VkGroup>,
) {
    private lateinit var attachments: List<VkAttachment>

    private val inflater = LayoutInflater.from(context)

    private val colorPrimary = ContextCompat.getColor(
        context,
        R.color.colorPrimary
    )
    private val colorBackground = ContextCompat.getColor(
        context,
        R.color.colorBackground
    )
    private val colorSecondary = ContextCompat.getColor(
        context,
        R.color.colorSecondary
    )

    private val timeReadBackground = ContextCompat.getDrawable(
        context,
        R.drawable.time_read_indicator_on_attachments_background
    )

    private var photoClickListener: ((url: String) -> Unit)? = null
    private var replyClickListener: ((replyMessage: VkMessage) -> Unit)? = null
    private var forwardsClickListener: ((forwards: List<VkMessage>) -> Unit)? = null

    private val displayMetrics get() = Resources.getSystem().displayMetrics

    fun withPhotoClickListener(block: ((url: String) -> Unit)?): AttachmentInflater {
        this.photoClickListener = block
        return this
    }

    fun withReplyClickListener(block: ((replyMessage: VkMessage) -> Unit)?): AttachmentInflater {
        this.replyClickListener = block
        return this
    }

    fun withForwardsClickListener(block: ((forwards: List<VkMessage>) -> Unit)?): AttachmentInflater {
        this.forwardsClickListener = block
        return this
    }

    fun inflate() {
        container.removeAllViews()
        replyContainer.removeAllViews()

        replyContainer.toggleVisibility(message.hasReply())
        container.toggleVisibility(
            !message.attachments.isNullOrEmpty()
                    || message.hasForwards()
                    || message.hasGeo()
        )

        timeReadContainer.run {
            updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val margin = (if (container.isVisible) 6 else 2).dpToPx()
                updateMarginsRelative(end = margin, bottom = margin)
            }

            background = if (container.isVisible) timeReadBackground else null
        }

        if (message.hasReply()) {
            reply(requireNotNull(message.replyMessage))
        }

        if (message.hasForwards()) {
            forwards(requireNotNull(message.forwards))
        }

        if (message.hasGeo()) {
            geo(requireNotNull(message.geo))
        }

        if (message.attachments.isNullOrEmpty()) return
        attachments = requireNotNull(message.attachments)

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

                else -> unknown(attachment)
            }
        }
    }

    private fun unknown(attachment: VkAttachment) {
        val attachmentType = attachment.javaClass.name
        Log.e(
            "Attachment inflater",
            "Unknown attachment type: $attachmentType"
        )

        val textView = AppCompatTextView(context)
        textView.text = attachmentType

        container.addView(textView)
    }

    private fun reply(replyMessage: VkMessage) {
        val binding = ItemMessageAttachmentReplyBinding.inflate(inflater, replyContainer, true)
        binding.root.setOnClickListener { replyClickListener?.invoke(replyMessage) }

        val attachmentText = (VkUtils.getAttachmentText(
            message = replyMessage
        ))?.parseString(context)

        val forwardsMessage = (if (replyMessage.text == null) VkUtils.getForwardsText(
            message = replyMessage
        ) else null)?.parseString(context)

        val messageText = attachmentText ?: forwardsMessage ?: (replyMessage.text.orDots()).run {
            VkUtils.prepareMessageText(this)
        }

        binding.text.text = VkUtils.visualizeMentions(
            messageText = messageText,
            mentionColor = colorPrimary
        )

        val replyUserGroup = VkUtils.getMessageUserGroup(replyMessage, profiles, groups)

        val fromUser: VkUser? = replyUserGroup.first
        val fromGroup: VkGroup? = replyUserGroup.second

        val title = VkUtils.getMessageTitle(replyMessage, fromUser, fromGroup)
        binding.title.text = title.orDots()
    }

    private fun forwards(forwards: List<VkMessage>) {
        val binding = ItemMessageAttachmentForwardsBinding.inflate(inflater, container, true)

        binding.root.setOnClickListener { forwardsClickListener?.invoke(forwards) }
    }

    private fun geo(geo: BaseVkMessage.Geo) {
        val binding = ItemMessageAttachmentGeoBinding.inflate(inflater, container, true)

        binding.location.text = geo.place.title
        binding.location.toggleVisibilityIfHasContent()
    }

    private fun photo(photo: VkPhoto) {
        val size = photo.getSizeOrSmaller(VkPhoto.SIZE_TYPE_807) ?: return

        val specRatio = size.width.toFloat() / size.height.toFloat()
        val widthMultiplier: Float = when {
            specRatio > 1 -> 0.7F
            specRatio < 1 -> 0.45F
            else -> 0.35F
        }
        val ratio = "${size.width}:${size.height}"

        val spacer = Space(context).apply {
            layoutParams =
                LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5.dpToPx())
        }

        if (container.isNotEmpty()) {
            container.addView(spacer)
        }

        val binding = ItemMessageAttachmentPhotoBinding.inflate(inflater, container, true)

        val cornersRadius = 17.dpToPx().toFloat()

        binding.border.run {
            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(cornersRadius)

            updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = (displayMetrics.widthPixels * widthMultiplier).roundToInt()
                dimensionRatio = ratio
            }
            loadWithGlide {
                imageDrawable = ColorDrawable(colorSecondary)
                loadPriority = Priority.IMMEDIATE
                cacheStrategy = DiskCacheStrategy.NONE
            }
        }

        binding.image.run {
            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(cornersRadius * 0.8F)

            setOnClickListener {
                photo.getMaxSize()?.let { size -> photoClickListener?.invoke(size.url) }
            }

            loadWithGlide {
                imageUrl = size.url
                crossFade = true
                placeholderDrawable = ColorDrawable(colorBackground)
                loadPriority = Priority.LOW
            }
        }
    }

    private fun video(video: VkVideo) {
        val spacer = Space(context).apply {
            layoutParams =
                LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5.dpToPx())
        }
        if (container.isNotEmpty()) {
            container.addView(spacer)
        }

        val size = video.imageForWidthAtLeast(300) ?: return
        val binding = ItemMessageAttachmentVideoBinding.inflate(inflater, container, true)

        val specRatio = size.width.toFloat() / size.height.toFloat()
        val widthMultiplier: Float = when {
            specRatio > 1 -> 0.7F
            specRatio < 1 -> 0.45F
            else -> 0.35F
        }
        val ratio = "${size.width}:${size.height}"

        val cornersRadius = 17.dpToPx().toFloat()

        binding.border.run {
            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(cornersRadius)

            updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = (displayMetrics.widthPixels * widthMultiplier).roundToInt()
                dimensionRatio = ratio
            }
            loadWithGlide {
                imageDrawable = ColorDrawable(colorSecondary)
                loadPriority = Priority.IMMEDIATE
                cacheStrategy = DiskCacheStrategy.NONE
            }
        }

        binding.image.run {
            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(cornersRadius * 0.8F)

            loadWithGlide {
                imageUrl = size.url
                crossFade = true
                placeholderDrawable = ColorDrawable(colorBackground)
                loadPriority = Priority.LOW
            }
        }
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
        val binding = ItemMessageAttachmentLinkBinding.inflate(
            inflater, container, true
        )

        binding.title.text = link.title
        binding.title.toggleVisibility(!link.title.isNullOrBlank())

        binding.caption.text = link.caption
        binding.caption.toggleVisibility(!link.caption.isNullOrBlank())

        link.photo?.getSizeOrSmaller('y')?.let { size ->
            binding.preview.loadWithGlide {
                imageUrl = size.url
                crossFade = true
            }
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

            loadWithGlide {
                imageUrl = url
                crossFade = true
            }
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

        val title = (when {
            group == null && user != null -> user.fullName
            user == null && group != null -> group.name
            else -> null
        }).orDots()

        binding.postTitle.text = context.getString(postTitleRes)
        binding.postTitle.gone()

        binding.avatar.toggleVisibility(group != null || user != null)

        if (binding.avatar.isVisible) {
            binding.avatar.loadWithGlide {
                imageUrl = avatar
                crossFade = true
            }
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
        val binding = ItemMessageAttachmentVoiceBinding.inflate(inflater, container, true)

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
        val binding = ItemMessageAttachmentCallBinding.inflate(inflater, container, true)

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

            loadWithGlide {
                imageUrl = url
                crossFade = true
            }
        }
    }

    private fun gift(gift: VkGift) {
        val binding = ItemMessageAttachmentGiftBinding.inflate(inflater, container, true)

        val url = gift.thumb256 ?: gift.thumb96 ?: gift.thumb48

        binding.image.run {
            val size = 140.dpToPx()

            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(12.dpToPx().toFloat())

            layoutParams = LinearLayoutCompat.LayoutParams(size, size)

            loadWithGlide {
                imageUrl = url
                crossFade = true
            }
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

        binding.dimmer.loadWithGlide {
            imageDrawable = dimmerDrawable
            transformations = listOf(TypeTransformations.RoundedCornerCrop(cornersRadius))
            loadPriority = Priority.IMMEDIATE
            cacheStrategy = DiskCacheStrategy.NONE
        }

        binding.image.run {
            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(cornersRadius.toFloat())

            loadWithGlide {
                imageUrl = photoUrl
                crossFade = true
                placeholderDrawable = ColorDrawable(Color.GRAY)
            }
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
