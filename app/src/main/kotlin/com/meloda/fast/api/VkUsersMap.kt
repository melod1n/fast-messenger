package com.meloda.fast.api

import com.meloda.fast.api.model.data.VkUserData
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

    fun messageUser(message: VkMessageDomain): VkUserDomain? =
        if (!message.isUser()) null
        else map[message.fromId]

    companion object {
        fun forUsers(users: List<VkUserData>): VkUsersMap =
            VkUsersMap(users = users.map(VkUserData::mapToDomain))
    }
}
