package com.meloda.app.fast.data

import com.meloda.app.fast.common.extensions.toHashMap
import com.meloda.app.fast.model.api.data.VkMessageData
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.VkGroupDomain
import com.meloda.app.fast.model.api.domain.VkMessage
import kotlin.math.abs

class VkGroupsMap(
    private val groups: List<VkGroupDomain>
) {

    private val map: HashMap<Int, VkGroupDomain> by lazy {
        groups.associateBy(VkGroupDomain::id).toHashMap()
    }

    fun groups(): List<VkGroupDomain> = map.values.toList()

    fun conversationGroup(conversation: VkConversation): VkGroupDomain? =
        if (!conversation.peerType.isGroup()) null
        else map[abs(conversation.id)]

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

    fun group(groupId: Int): VkGroupDomain? = map[abs(groupId)]

    companion object {

        fun forGroups(groups: List<VkGroupDomain>): VkGroupsMap = VkGroupsMap(groups = groups)

        fun List<VkGroupDomain>.toGroupsMap(): VkGroupsMap = forGroups(this)
    }
}
