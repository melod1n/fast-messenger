package com.meloda.fast.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.database.CacheStorage
import com.meloda.fast.api.VKApi
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.model.VKMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatsViewModel : ViewModel() {

    val chatsAnswer = MutableLiveData<ChatsAnswer?>()

    data class ChatsAnswer(var status: Status, var message: String? = "") {
        companion object {
            val SUCCESS get() = ChatsAnswer(Status.SUCCESS)
            val FAIL get() = ChatsAnswer(Status.FAIL)
            val LOADING get() = ChatsAnswer(Status.LOADING)
        }

        enum class Status {
            SUCCESS, FAIL, LOADING
        }
    }

    fun loadChats() = viewModelScope.launch(Dispatchers.IO) {
        chatsAnswer.postValue(ChatsAnswer.LOADING)

        try {
            val chats = VKApi.messages()
                .getConversations()
                .filter("all")
                .extended(true)
                .fields(VKConstants.USER_FIELDS + ',' + VKConstants.GROUP_FIELDS)
                .offset(0)
                .count(30)
                .executeSuspend(VKConversation::class.java)

//            CacheStorage.chatsStorage.insertValues(chats)

            val lastMessages = arrayListOf<VKMessage>()
            chats.collect {
                lastMessages.add(it.lastMessage)
            }

            CacheStorage.messagesStorage.insertValues(lastMessages)
            CacheStorage.usersStorage.insertValues(VKConversation.profiles)
            CacheStorage.groupsStorage.insertValues(VKConversation.groups)
//
//            chatsAnswer.value = ChatsAnswer.SUCCESS

            chatsAnswer.postValue(ChatsAnswer.SUCCESS)

            withContext(Dispatchers.Main) {
//                adapter.updateValues(chats.toList())
//                adapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            chatsAnswer.postValue(ChatsAnswer.FAIL.also { it.message = e.message })
//            chatsAnswer.value = ChatsAnswer.FAIL.also { it.message = e.message }
        }
    }
}