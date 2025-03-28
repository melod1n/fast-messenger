package dev.meloda.fast.messageshistory.model

import dev.meloda.fast.model.api.domain.VkMessage

sealed class MessageDialog {
    data class MessageOptions(val message: VkMessage) : MessageDialog()
    data class MessagePin(val messageId: Int) : MessageDialog()
    data class MessageUnpin(val messageId: Int) : MessageDialog()
    data class MessageDelete(val message: VkMessage) : MessageDialog()
    data class MessagesDelete(val messages: List<VkMessage>) : MessageDialog()

    data class MessageSpam(
        val message: VkMessage,
        val isSpam: Boolean
    ) : MessageDialog()

    data class MessageMarkImportance(
        val message: VkMessage,
        val isImportant: Boolean
    ) : MessageDialog()
}
