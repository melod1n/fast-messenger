package com.meloda.fast.screens.conversations

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.datasource.ConversationsDataSource
import com.meloda.fast.api.datasource.UsersDataSource
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.network.request.ConversationsGetRequest
import com.meloda.fast.api.network.request.UsersGetRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val dataSource: ConversationsDataSource,
    private val usersDataSource: UsersDataSource
) : BaseViewModel() {

    fun loadConversations() = viewModelScope.launch(Dispatchers.Default) {
        makeJob({
            dataSource.getAllChats(
                ConversationsGetRequest(
                    count = 30,
//                    offset = 177,
                    extended = true,
                    fields = "${VKConstants.USER_FIELDS},${VKConstants.GROUP_FIELDS}"
                )
            )
        },
            onAnswer = {
                it.response?.let { response ->
                    val profiles = hashMapOf<Int, VkUser>()
                    response.profiles?.forEach { baseUser ->
                        baseUser.asVkUser().let { user -> profiles[user.id] = user }
                    }

                    val groups = hashMapOf<Int, VkGroup>()
                    response.groups?.forEach { baseGroup ->
                        baseGroup.asVkGroup().let { group -> groups[group.id] = group }
                    }

                    sendEvent(
                        ConversationsLoaded(
                            count = response.count,
                            unreadCount = response.unreadCount ?: 0,
                            conversations = response.items.map { items ->
                                items.conversation.asVkConversation(
                                    items.lastMessage?.asVkMessage()
                                )
                            },
                            profiles = profiles,
                            groups = groups
                        )
                    )
                }
            },
            onError = {
                val er = it
                throw it
            },
            onStart = { sendEvent(StartProgressEvent) },
            onEnd = { sendEvent(StopProgressEvent) })
    }

    fun loadProfileUser() = viewModelScope.launch {
        makeJob({
            usersDataSource.getById(UsersGetRequest(fields = "online,photo_200"))
        },
            onAnswer = {
                it.response?.let { r ->
                    val users = r.map { u -> u.asVkUser() }
                    usersDataSource.storeUsers(users)

                    UserConfig.vkUser.value = users[0]
                }
            })
    }
}

data class ConversationsLoaded(
    val count: Int,
    val unreadCount: Int?,
    val conversations: List<VkConversation>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>
) : VKEvent()
