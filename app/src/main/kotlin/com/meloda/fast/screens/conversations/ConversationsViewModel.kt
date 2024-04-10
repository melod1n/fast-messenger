package com.meloda.fast.screens.conversations

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import com.google.common.collect.ImmutableList
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.processState
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.account.domain.usecase.AccountUseCase
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.emitOnScope
import com.meloda.fast.ext.findIndex
import com.meloda.fast.ext.findWithIndex
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.setValue
import com.meloda.fast.screens.conversations.domain.usecase.ConversationsUseCase
import com.meloda.fast.screens.conversations.model.ConversationOption
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import com.meloda.fast.screens.conversations.model.ConversationsShowOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
