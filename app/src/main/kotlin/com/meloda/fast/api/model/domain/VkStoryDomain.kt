package com.meloda.fast.api.model.domain

data class VkStoryDomain(
    val id: Int,
    val ownerId: Int,
    val date: Int,
    val photo: VkPhotoDomain?
) : VkAttachment {

    fun isFromUser() = ownerId > 0

    fun isFromGroup() = ownerId < 0

    val className: String = this::class.java.name
}
