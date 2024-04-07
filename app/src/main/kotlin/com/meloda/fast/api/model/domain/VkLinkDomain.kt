package com.meloda.fast.api.model.domain

data class VkLinkDomain(
    val url: String,
    val title: String?,
    val caption: String?,
    val photo: VkPhotoDomain?,
    val target: String?,
    val isFavorite: Boolean
) : VkAttachment {

    val className: String = this::class.java.name
}
