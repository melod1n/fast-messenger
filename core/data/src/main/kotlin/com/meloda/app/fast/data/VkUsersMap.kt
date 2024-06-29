package com.meloda.app.fast.data

import com.meloda.app.fast.model.api.data.VkMessageData
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.model.api.domain.VkUserDomain

class VkUsersMap(
    private val users: List<VkUserDomain>
) {

    private val map: HashMap<Int, VkUserDomain> by lazy {
        HashMap(users.associateBy(VkUserDomain::id))
    }

    fun users(): List<VkUserDomain> = map.values.toList()

    fun conversationUser(conversation: VkConversation): VkUserDomain? =
        if (!conversation.peerType.isUser()) null
        else map[conversation.id]

    fun messageActionUser(message: VkMessage): VkUserDomain? =
        if (message.actionMemberId == null || message.actionMemberId!! <= 0) null
        else map[message.actionMemberId]

    fun messageActionUser(message: VkMessageData): VkUserDomain? =
        if (message.action?.memberId == null || message.action!!.memberId!! <= 0) null
        else map[message.action!!.memberId]

    fun messageUser(message: VkMessage): VkUserDomain? =
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
