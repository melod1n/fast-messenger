package com.meloda.fast.api.model.attachments

import android.os.Parcelable
import com.meloda.fast.model.DataItem
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
open class VkAttachment : DataItem<Int>(), Parcelable {

    @IgnoredOnParcel
    override val dataItemId: Int = -1

    open fun asString(withAccessKey: Boolean = true) = ""

}