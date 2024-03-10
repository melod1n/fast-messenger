package com.meloda.fast.api.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkChatMemberDomain(
    val memberId: Int,
    val invitedBy: Int,
    val joinDate: Int,
    val isAdmin: Boolean,
    val isOwner: Boolean,
    val canKick: Boolean
) : Parcelable
