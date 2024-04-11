package com.meloda.fast.screens.conversations

import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.screens.conversations.model.ConversationOption
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import kotlinx.coroutines.flow.StateFlow

interface ConversationsViewModel {

    val screenState: StateFlow<ConversationsScreenState>

    fun onDeleteDialogDismissed()

    fun onDeleteDialogPositiveClick(conversationId: Int)

    fun onRefresh()

    fun onConversationItemLongClick(conversation: VkConversationUi)

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick(conversation: VkConversationUi)
    fun onOptionClicked(conversation: VkConversationUi, option: ConversationOption)
}
