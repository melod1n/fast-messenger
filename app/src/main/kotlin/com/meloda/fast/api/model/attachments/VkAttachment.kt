package com.meloda.fast.api.model.attachments

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

open class VkAttachment {

    open fun asString(withAccessKey: Boolean = true) = ""
}
