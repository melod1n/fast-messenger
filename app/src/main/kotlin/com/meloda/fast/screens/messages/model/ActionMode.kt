package com.meloda.fast.screens.messages.model

sealed class ActionMode {

    data object Send : ActionMode()
    data object Record : ActionMode()
    data object Edit : ActionMode()
    data object Delete : ActionMode()
}
