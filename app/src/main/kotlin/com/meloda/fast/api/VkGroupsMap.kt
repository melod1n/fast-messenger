package com.meloda.fast.api

import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.ext.toMap

class VkGroupsMap(
    private val groups: List<VkGroupDomain>
) {

    private val map: HashMap<Int, VkGroupDomain> by lazy {
        groups.toMap(hashMapOf(), VkGroupDomain::id)
    }

    fun groups(): List<VkGroupDomain> = map.values.toList()

    fun conversationGroup(conversation: VkConversationDomain): VkGroupDomain? =
        if (!conversation.isUser()) null
        else map[conversation.id]

    fun messageActionGroup(message: VkMessageDomain): VkGroupDomain? =
        if (message.actionMemberId == null || message.actionMemberId <= 0) null
        else map[message.actionMemberId]

    fun messageGroup(message: VkMessageDomain): VkGroupDomain? =
        if (!message.isUser()) null
        else map[message.fromId]

    fun group(groupId: Int): VkGroupDomain? = map[groupId]

    companion object {

        fun forGroups(groups: List<VkGroupDomain>): VkGroupsMap = VkGroupsMap(groups = groups)

        fun List<VkGroupDomain>.toGroupsMap(): VkGroupsMap = forGroups(this)
    }
}
