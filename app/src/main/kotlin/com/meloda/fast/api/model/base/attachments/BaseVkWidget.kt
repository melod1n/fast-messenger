package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkWidget
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
data class BaseVkWidget(val id: Int) : BaseVkAttachment() {

    fun asVkWidget() = VkWidget(id)

}
