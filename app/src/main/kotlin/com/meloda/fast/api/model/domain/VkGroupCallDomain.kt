package com.meloda.fast.api.model.domain

data class VkGroupCallDomain(
    val initiatorId: Int
) : VkAttachment {

    val className: String = this::class.java.name
}
