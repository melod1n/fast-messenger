package com.meloda.fast.screens.chatinfo

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VkChat
import com.meloda.fast.api.model.VkChatMember
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.data.messages.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatInfoViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository
) : BaseViewModel() {

    fun getChatInfo(chatId: Int) = viewModelScope.launch {
        makeJob(
            { messagesRepository.getChat(chatId, VKConstants.ALL_FIELDS) },
            onAnswer = {
                val response = it.response ?: return@makeJob
                val chat = response.asVkChat()

                sendEvent(GetChatInfoEvent(chat))
            }
        )
    }

    fun getConversationMembers(peerId: Int) = viewModelScope.launch {
        makeJob(
            {
                messagesRepository.getConversationMembers(
                    peerId,
                    extended = true,
                    fields = VKConstants.ALL_FIELDS
                )
            },
            onAnswer = {
                val response = it.response ?: return@makeJob

                val items = response.items.map { member -> member.asVkChatMember() }
                val profiles = response.profiles.orEmpty().map { profile -> profile.asVkUser() }
                val groups = response.groups.orEmpty().map { group -> group.asVkGroup() }

                sendEvent(GetConversationMembersEvent(response.count, items, profiles, groups))
            }
        )
    }

}

data class GetConversationMembersEvent(
    val count: Int,
    val items: List<VkChatMember>,
    val profiles: List<VkUser>,
    val groups: List<VkGroup>
) : VkEvent()

data class GetChatInfoEvent(val chat: VkChat) : VkEvent()