package com.meloda.app.fast.messageshistory.validation

import com.meloda.app.fast.common.extensions.addIf
import com.meloda.app.fast.messageshistory.model.MessagesHistoryScreenState
import com.meloda.app.fast.messageshistory.model.MessagesHistoryValidationResult

class MessagesHistoryValidator {

    fun validate(screenState: MessagesHistoryScreenState): List<MessagesHistoryValidationResult> {
        val results = mutableListOf<MessagesHistoryValidationResult>()

        results.addIf(MessagesHistoryValidationResult.MessageEmpty) {
            screenState.message.isBlank()
        }

        results.addIf(MessagesHistoryValidationResult.AttachmentsEmpty) {
            screenState.attachments.isEmpty()
        }

        if (results.size == 2) {
            results += MessagesHistoryValidationResult.Empty
        }

        if (results.isEmpty()) {
            return listOf(MessagesHistoryValidationResult.Valid)
        }

        return results
    }
}
