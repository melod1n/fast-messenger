package com.meloda.fast.screens.chatinfo

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VkChat
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

}

data class GetChatInfoEvent(val chat: VkChat) : VkEvent()