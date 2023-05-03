package com.meloda.fast.api.model.attachments

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
open class VkAttachment : Parcelable {

    open fun asString(withAccessKey: Boolean = true) = ""

}
