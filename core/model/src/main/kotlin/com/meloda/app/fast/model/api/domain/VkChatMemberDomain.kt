package com.meloda.app.fast.model.api.domain

data class VkChatMemberDomain(
    val memberId: Int,
    val invitedBy: Int,
    val joinDate: Int,
    val isAdmin: Boolean,
    val isOwner: Boolean,
    val canKick: Boolean
)
