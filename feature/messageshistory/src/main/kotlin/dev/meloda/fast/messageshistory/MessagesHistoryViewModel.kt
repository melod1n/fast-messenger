package dev.meloda.fast.messageshistory

import android.os.Bundle
import androidx.compose.ui.text.input.TextFieldValue
import android.net.Uri
import dev.meloda.fast.messageshistory.model.MessageDialog
import dev.meloda.fast.messageshistory.model.MessageNavigation
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.VoicePlaybackState
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkAudioMessageDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkStickerPackDomain
import dev.meloda.fast.ui.model.vk.MessageUiItem
import kotlinx.coroutines.flow.StateFlow

interface MessagesHistoryViewModel {

    val screenState: StateFlow<MessagesHistoryScreenState>
    val navigation: StateFlow<MessageNavigation?>
    val messages: StateFlow<List<VkMessage>>
    val uiMessages: StateFlow<List<MessageUiItem>>
    val dialog: StateFlow<MessageDialog?>
    val selectedMessages: StateFlow<List<VkMessage>>

    val showKeyboard: StateFlow<Boolean>

    val isNeedToScrollToIndex: StateFlow<Int?>

    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>

    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    val voicePlayback: StateFlow<VoicePlaybackState>

    val isRecordingVoice: StateFlow<Boolean>
    val voiceRecordingDurationSec: StateFlow<Int>
    val isRecordingVideoMessage: StateFlow<Boolean>

    val stickerPacks: StateFlow<List<VkStickerPackDomain>>
    val isStickerPickerOpen: StateFlow<Boolean>

    val isAttachmentPickerOpen: StateFlow<Boolean>
    val pickPhotoRequest: StateFlow<Int>

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
    fun onEmojiButtonClicked()
    fun onEmojiButtonLongClicked()
    fun onStickerPickerDismissed()
    fun onSendSticker(stickerId: Long)
    fun onEmojiSelected(emoji: String)

    fun onAttachmentPickerDismissed()
    fun onPickPhotoClicked()
    fun onSendPhoto(uri: Uri)
    fun onActionButtonClicked()
    fun onRecordStart()
    fun onRecordFinish()
    fun onRecordCancel()
    fun onStartVideoMessageRecord()
    fun onCancelVideoMessageRecord()
    fun onSendVideoMessage(file: java.io.File, durationSec: Int)

    fun onPaginationConditionsMet()

    fun onMessageClicked(messageId: Long)
    fun onMessageLongClicked(messageId: Long)

    fun onPinnedMessageClicked(messageId: Long)
    fun onUnpinMessageClicked()

    fun onEditSelectedMessageClicked()
    fun onDeleteSelectedMessagesClicked()

    fun onBoldClicked()
    fun onItalicClicked()
    fun onUnderlineClicked()
    fun onLinkClicked()
    fun onRegularClicked()

    fun onReplyCloseClicked()

    fun onRequestReplyToMessage(cmId: Long)

    fun onKeyboardShown()

    fun onVoiceMessageClicked(attachment: VkAudioMessageDomain)

    fun onMessageSeen(messageId: Long, cmId: Long)

    suspend fun loadMessageReadPeers(peerId: Long, cmId: Long): Int
}
