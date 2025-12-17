package dev.meloda.fast.data

import dev.meloda.fast.model.api.data.VkMessageData
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.model.api.domain.VkGroupDomain
import dev.meloda.fast.model.api.domain.VkMessage
import kotlin.math.abs

class VkGroupsMap(
    private val groups: List<VkGroupDomain>
) {

    private val map: HashMap<Long, VkGroupDomain> by lazy {
        HashMap(groups.associateBy(VkGroupDomain::id))
    }

    fun groups(): List<VkGroupDomain> = map.values.toList()

    fun convoGroup(convo: VkConvo): VkGroupDomain? =
        if (!convo.peerType.isGroup()) null
        else map[abs(convo.id)]

    fun messageActionGroup(message: VkMessage): VkGroupDomain? =
        if (message.actionMemberId == null || message.actionMemberId!! >= 0) null
        else map[abs(message.actionMemberId!!)]

    fun messageActionGroup(message: VkMessageData): VkGroupDomain? =
        if (message.action?.memberId == null || message.action!!.memberId!! >= 0) null
        else map[abs(message.action!!.memberId!!)]

    fun messageGroup(message: VkMessage): VkGroupDomain? =
        if (!message.isGroup()) null
        else map[abs(message.fromId)]

    fun messageGroup(message: VkMessageData): VkGroupDomain? =
        if (message.fromId >= 0) null
        else map[abs(message.fromId)]

    fun group(groupId: Long): VkGroupDomain? = map[abs(groupId)]

    companion object {

        fun forGroups(groups: List<VkGroupDomain>): VkGroupsMap = VkGroupsMap(groups = groups)

        fun List<VkGroupDomain>.toGroupsMap(): VkGroupsMap = forGroups(this)
    }
}
