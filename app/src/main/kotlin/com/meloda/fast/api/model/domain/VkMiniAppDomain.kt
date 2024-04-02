package com.meloda.fast.api.model.domain

data class VkMiniAppDomain(
    val link: String
) : VkAttachment {

    val className: String = this::class.java.name
}
