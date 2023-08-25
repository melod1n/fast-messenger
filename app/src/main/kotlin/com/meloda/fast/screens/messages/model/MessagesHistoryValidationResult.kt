package com.meloda.fast.screens.messages.model

sealed interface MessagesHistoryValidationResult {

    data object Empty : MessagesHistoryValidationResult
    data object AttachmentsEmpty : MessagesHistoryValidationResult
    data object MessageEmpty : MessagesHistoryValidationResult
    data object Valid : MessagesHistoryValidationResult
}
