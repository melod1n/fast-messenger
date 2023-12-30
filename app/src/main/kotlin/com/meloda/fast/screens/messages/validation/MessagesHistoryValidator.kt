package com.meloda.fast.screens.messages.validation

import com.meloda.fast.ext.addIf
import com.meloda.fast.screens.messages.model.MessagesHistoryScreenState
import com.meloda.fast.screens.messages.model.MessagesHistoryValidationResult

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
