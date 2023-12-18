package com.meloda.fast.api.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.meloda.fast.api.model.attachments.VkAttachment
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class AttachmentList(
    val attachments: List<VkAttachment>
) : Parcelable {

    companion object {
        val EMPTY = AttachmentList(attachments = emptyList())
    }
}
