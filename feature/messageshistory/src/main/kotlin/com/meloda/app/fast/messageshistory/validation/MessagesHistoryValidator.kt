package dev.meloda.fast.messageshistory.validation

import dev.meloda.fast.common.extensions.addIf
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.MessagesHistoryValidationResult

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
