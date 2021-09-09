package com.meloda.fast.api.model.attachments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVKAttachmentItem(
    val type: String,
    val photo: VKPhotoAttachment?,
    val video: VKVideoAttachment?,
    val audio: VKAudioAttachment?,
    val doc: VKFileAttachment?,
    val link: VKLinkAttachment?
) : Parcelable

abstract class BaseVKAttachment : Parcelable