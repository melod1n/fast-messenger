package com.meloda.fast.api.model.domain

data class VkWidgetDomain(
    val id: Int
) : VkAttachment {

    val className: String = this::class.java.name
}
