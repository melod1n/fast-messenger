package dev.meloda.fast.messageshistory

import android.os.Bundle
import androidx.compose.ui.text.input.TextFieldValue
import dev.meloda.fast.messageshistory.model.MessageDialog
import dev.meloda.fast.messageshistory.model.MessageNavigation
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkMessage
import kotlinx.coroutines.flow.StateFlow

interface MessagesHistoryViewModel {

    val screenState: StateFlow<MessagesHistoryScreenState>
    val navigation: StateFlow<MessageNavigation?>
    val messages: StateFlow<List<VkMessage>>
    val uiMessages: StateFlow<List<UiItem>>
    val dialog: StateFlow<MessageDialog?>
    val selectedMessages: StateFlow<List<VkMessage>>

    val inputFieldFocusRequester: StateFlow<Boolean>

    val isNeedToScrollToIndex: StateFlow<Int?>

    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>

    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onNavigationConsumed()

    fun onTopBarClicked()

    fun onDialogConfirmed(dialog: MessageDialog, bundle: Bundle)
    fun onDialogDismissed(dialog: MessageDialog)
    fun onDialogItemPicked(dialog: MessageDialog, bundle: Bundle)

    fun onScrolledToIndex()

    fun onCloseButtonClicked()
    fun onRefresh()
    fun onAttachmentButtonClicked()
    fun onMessageInputChanged(newText: TextFieldValue)
    fun onEmojiButtonLongClicked()
    fun onActionButtonClicked()

    fun onPaginationConditionsMet()

    fun onMessageClicked(messageId: Long)
    fun onMessageLongClicked(messageId: Long)

    fun onPinnedMessageClicked(messageId: Long)
    fun onUnpinMessageClicked()

    fun onDeleteSelectedMessagesClicked()

    fun onBoldClicked()
    fun onItalicClicked()
    fun onUnderlineClicked()
    fun onLinkClicked()
    fun onRegularClicked()

    fun onReplyCloseClicked()

    fun onRequestReplyToMessage(cmId: Long)

    suspend fun loadMessageReadPeers(peerId: Long, cmId: Long): Int
}
