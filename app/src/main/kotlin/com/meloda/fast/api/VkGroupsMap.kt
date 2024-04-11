package com.meloda.fast.api

import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.ext.toMap
import kotlin.math.abs

class VkGroupsMap(
    private val groups: List<VkGroupDomain>
) {

    private val map: HashMap<Int, VkGroupDomain> by lazy {
        groups.toMap(hashMapOf(), VkGroupDomain::id)
    }

    fun groups(): List<VkGroupDomain> = map.values.toList()

    fun conversationGroup(conversation: VkConversationDomain): VkGroupDomain? =
        if (!conversation.isGroup()) null
        else map[abs(conversation.id)]

    fun messageActionGroup(message: VkMessageDomain): VkGroupDomain? =
        if (message.actionMemberId == null || message.actionMemberId <= 0) null
        else map[abs(message.actionMemberId)]

    fun messageGroup(message: VkMessageDomain): VkGroupDomain? =
        if (!message.isGroup()) null
        else map[abs(message.fromId)]

    fun group(groupId: Int): VkGroupDomain? = map[abs(groupId)]

    companion object {

        fun forGroups(groups: List<VkGroupDomain>): VkGroupsMap = VkGroupsMap(groups = groups)

        fun List<VkGroupDomain>.toGroupsMap(): VkGroupsMap = forGroups(this)
    }
}
