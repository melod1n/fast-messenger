package dev.meloda.fast.data

import dev.meloda.fast.model.api.data.VkMessageData
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkUser

class VkUsersMap(
    private val users: List<VkUser>
) {

    private val map: HashMap<Long, VkUser> by lazy {
        HashMap(users.associateBy(VkUser::id))
    }

    fun users(): List<VkUser> = map.values.toList()

    fun convoUser(convo: VkConvo): VkUser? =
        if (!convo.peerType.isUser()) null
        else map[convo.id]

    fun messageActionUser(message: VkMessage): VkUser? =
        if (message.actionMemberId == null || message.actionMemberId!! <= 0) null
        else map[message.actionMemberId]

    fun messageActionUser(message: VkMessageData): VkUser? =
        if (message.action?.memberId == null || message.action!!.memberId!! <= 0) null
        else map[message.action!!.memberId]

    fun messageUser(message: VkMessage): VkUser? =
        if (!message.isUser()) null
        else map[message.fromId]

    fun messageUser(message: VkMessageData): VkUser? =
        if (message.fromId > 0) map[message.fromId]
        else null

    fun user(userId: Long): VkUser? = map[userId]

    companion object {

        fun forUsers(users: List<VkUser>): VkUsersMap = VkUsersMap(users = users)

        fun List<VkUser>.toUsersMap(): VkUsersMap = forUsers(this)
    }
}
