package com.meloda.fast.screens.messages.model

sealed interface MessagesHistoryActionButtonMode {

    data object Send : MessagesHistoryActionButtonMode
    data object Record : MessagesHistoryActionButtonMode
    data object Edit : MessagesHistoryActionButtonMode
    data object Delete : MessagesHistoryActionButtonMode
}
