package com.meloda.fast.common

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.screens.chatinfo.ChatInfoFragment
import com.meloda.fast.screens.conversations.ConversationsFragment
import com.meloda.fast.screens.login.LoginFragment
import com.meloda.fast.screens.main.MainFragment
import com.meloda.fast.screens.messages.ForwardedMessagesFragment
import com.meloda.fast.screens.messages.MessagesHistoryFragment
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.screens.updates.UpdatesFragment
import com.meloda.fast.screens.userbanned.UserBannedFragment

@Suppress("FunctionName")
object Screens {
    fun Main() = FragmentScreen { MainFragment() }

    fun Login() = FragmentScreen { LoginFragment.newInstance() }

    fun Conversations() = FragmentScreen { ConversationsFragment() }

    fun MessagesHistory(
        conversation: VkConversation,
        user: VkUser?,
        group: VkGroup?
    ) = FragmentScreen { MessagesHistoryFragment.newInstance(conversation, user, group) }

    fun ForwardedMessages(
        conversation: VkConversation,
        messages: List<VkMessage>,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf()
    ) = FragmentScreen {
        ForwardedMessagesFragment.newInstance(
            conversation, messages, profiles, groups
        )
    }

    fun ChatInfo(
        conversation: VkConversation,
        user: VkUser?,
        group: VkGroup?
    ) = FragmentScreen { ChatInfoFragment.newInstance(conversation, user, group) }

    fun Updates(updateItem: UpdateItem? = null) =
        FragmentScreen { UpdatesFragment.newInstance(updateItem) }

    fun Settings() = FragmentScreen { SettingsFragment() }

    fun UserBanned(
        memberName: String,
        message: String,
        restoreUrl: String,
        accessToken: String
    ) = FragmentScreen {
        UserBannedFragment.newInstance(
            memberName, message, restoreUrl, accessToken
        )
    }
}
