package com.meloda.fast.api

import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.ext.toMap

class VkUsersMap(
    private val users: List<VkUserDomain>
) {

    private val map: HashMap<Int, VkUserDomain> by lazy {
        users.toMap(hashMapOf(), VkUserDomain::id)
    }

    fun users(): List<VkUserDomain> = map.values.toList()

    fun conversationUser(conversation: VkConversationDomain): VkUserDomain? =
        if (!conversation.isUser()) null
        else map[conversation.id]

    fun messageActionUser(message: VkMessageDomain): VkUserDomain? =
        if (message.actionMemberId == null || message.actionMemberId <= 0) null
        else map[message.actionMemberId]

    fun messageActionUser(message: VkMessageData): VkUserDomain? =
        if (message.action?.memberId == null || message.action.memberId <= 0) null
        else map[message.action.memberId]

    fun messageUser(message: VkMessageDomain): VkUserDomain? =
        if (!message.isUser()) null
        else map[message.fromId]

    fun messageUser(message: VkMessageData): VkUserDomain? =
        if (message.fromId > 0) map[message.fromId]
        else null

    fun user(userId: Int): VkUserDomain? = map[userId]

    companion object {

        fun forUsers(users: List<VkUserDomain>): VkUsersMap = VkUsersMap(users = users)

        fun List<VkUserDomain>.toUsersMap(): VkUsersMap = forUsers(this)
    }
}
