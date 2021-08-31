package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
import com.meloda.fast.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationsViewModel : BaseViewModel() {

    fun loadConversations() = viewModelScope.launch(Dispatchers.Default) {

    }
}