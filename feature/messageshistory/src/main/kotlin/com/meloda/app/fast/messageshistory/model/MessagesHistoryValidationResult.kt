package com.meloda.app.fast.messageshistory.model

sealed interface MessagesHistoryValidationResult {

    data object Empty : MessagesHistoryValidationResult
    data object AttachmentsEmpty : MessagesHistoryValidationResult
    data object MessageEmpty : MessagesHistoryValidationResult
    data object Valid : MessagesHistoryValidationResult
}
