package com.meloda.fast.api.model.domain

data class VkGiftDomain(
    val id: Int,
    val thumb256: String?,
    val thumb96: String?,
    val thumb48: String
) : VkAttachment {

    val className: String = this::class.java.name
}
