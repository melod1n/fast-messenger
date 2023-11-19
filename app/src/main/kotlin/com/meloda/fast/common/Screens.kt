package com.meloda.fast.common

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.screens.messages.presentation.ForwardedMessagesFragment
import com.meloda.fast.screens.updates.UpdatesFragment

@Suppress("FunctionName")
object Screens {

    fun ForwardedMessages(
        conversation: VkConversationDomain,
        messages: List<VkMessage>,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf()
    ) = FragmentScreen {
        ForwardedMessagesFragment.newInstance(
            conversation, messages, profiles, groups
        )
    }

    fun Updates(updateItem: UpdateItem? = null) =
        FragmentScreen { UpdatesFragment.newInstance(updateItem) }
}
