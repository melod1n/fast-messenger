package com.meloda.app.fast.messageshistory.model

sealed class UiAction {
    data object OpenChatMaterials : UiAction()
    data object BackClicked : UiAction()
}
