package com.meloda.fast.model

import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
open class SelectableItem : Parcelable {

    @Ignore
    @IgnoredOnParcel
    var isSelected: Boolean = false

}