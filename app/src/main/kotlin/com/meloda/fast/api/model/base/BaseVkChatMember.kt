package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.meloda.fast.api.model.VkChatMember
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkChatMember(
    val member_id: Int,
    val invited_by: Int,
    val join_date: Int,
    val is_admin: Boolean?,
    val is_owner: Boolean?,
    val can_kick: Boolean?
) : Parcelable {

    fun asVkChatMember() = VkChatMember(
        memberId = member_id,
        invitedBy = invited_by,
        joinDate = join_date,
        isAdmin = is_admin == true,
        isOwner = is_owner == true,
        canKick = can_kick == true
    )

}