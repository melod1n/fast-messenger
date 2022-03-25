package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkWidget
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkWidget(val id: Int) : BaseVkAttachment() {

    fun asVkWidget() = VkWidget(id)

}