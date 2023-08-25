package com.meloda.fast.common

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.screens.chatinfo.ChatInfoFragment
import com.meloda.fast.screens.conversations.presentation.ConversationsFragment
import com.meloda.fast.screens.login.LoginFragment
import com.meloda.fast.screens.main.MainFragment
import com.meloda.fast.screens.messages.presentation.ForwardedMessagesFragment
import com.meloda.fast.screens.settings.presentation.SettingsFragment
import com.meloda.fast.screens.updates.UpdatesFragment
import com.meloda.fast.screens.userbanned.UserBannedFragment

@Suppress("FunctionName")
object Screens {
    fun Main() = FragmentScreen { MainFragment.newInstance() }

    fun Login() = FragmentScreen { LoginFragment.newInstance() }

    fun Conversations() = FragmentScreen { ConversationsFragment() }

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

    fun ChatInfo(
        conversation: VkConversationDomain,
        user: VkUser?,
        group: VkGroup?
    ) = FragmentScreen { ChatInfoFragment.newInstance(conversation, user, group) }

    fun Updates(updateItem: UpdateItem? = null) =
        FragmentScreen { UpdatesFragment.newInstance(updateItem) }

    fun Settings() = FragmentScreen { SettingsFragment.newInstance() }

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
