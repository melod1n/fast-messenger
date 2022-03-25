package com.meloda.fast.model

import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
open class SelectableItem constructor(
    @Ignore
    val selectableItemId: Int = 0
) : DataItem<Int>(), Parcelable {

    @Ignore
    @IgnoredOnParcel
    var isSelected: Boolean = false

    @Ignore
    @IgnoredOnParcel
    override val dataItemId = selectableItemId

}