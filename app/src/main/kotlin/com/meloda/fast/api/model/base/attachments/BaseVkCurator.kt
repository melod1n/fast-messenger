package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkCurator
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkCurator(
    val id: Int,
    val name: String,
    val description: String,
    val url: String,
    val photo: List<Photo>
) : BaseVkAttachment() {

    fun asVkCurator() = VkCurator(
        id = id
    )

    @Parcelize
    data class Photo(
        val height: Int,
        val url: String,
        val width: String
    ) : Parcelable

}