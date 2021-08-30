package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
import com.meloda.fast.base.viewmodel.BaseVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationsVM : BaseVM() {

    fun loadConversations() = viewModelScope.launch(Dispatchers.Default) {

    }
}