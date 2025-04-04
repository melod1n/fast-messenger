package dev.meloda.fast.model.api.domain

data class VkChatMemberDomain(
    val memberId: Long,
    val invitedBy: Int,
    val joinDate: Int,
    val isAdmin: Boolean,
    val isOwner: Boolean,
    val canKick: Boolean
)
