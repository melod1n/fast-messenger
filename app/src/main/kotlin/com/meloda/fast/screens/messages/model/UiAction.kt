package com.meloda.fast.screens.messages.model

sealed class UiAction {
    data object OpenChatMaterials : UiAction()
    data object BackClicked : UiAction()
}
