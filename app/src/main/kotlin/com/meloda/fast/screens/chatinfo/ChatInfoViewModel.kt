package com.meloda.fast.screens.chatinfo

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKConstants
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
                val profiles = response.profiles.orEmpty().map { profile -> profile.mapToDomain() }
                val groups = response.groups.orEmpty().map { group -> group.mapToDomain() }

                sendEvent(GetConversationMembersEvent(response.count, items, profiles, groups))
            }
        )
    }

    fun removeChatUser(chatId: Int, memberId: Int) = viewModelScope.launch {
        makeJob(
            { messagesRepository.removeChatUser(chatId, memberId) },
            onAnswer = {
                sendEvent(RemoveChatUserEvent(chatId, memberId))
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

data class RemoveChatUserEvent(
    val chatId: Int, val memberId: Int
) : VkEvent()
