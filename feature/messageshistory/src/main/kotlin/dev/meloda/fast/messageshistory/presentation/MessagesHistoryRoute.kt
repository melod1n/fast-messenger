package dev.meloda.fast.messageshistory.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.messageshistory.MessagesHistoryViewModel
import dev.meloda.fast.messageshistory.MessagesHistoryViewModelImpl
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.messageshistory.model.MessageNavigation
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.common.ImmutableList.Companion.toImmutableList
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MessagesHistoryRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onNavigateToChatMaterials: (peerId: Long, cmId: Long) -> Unit,
    onNavigateToPhotoViewer: (images: List<String>, index: Int) -> Unit,
    onNavigateToProfile: (userId: Long) -> Unit = {},
    viewModel: MessagesHistoryViewModel = koinViewModel<MessagesHistoryViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigation.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val uiMessages by viewModel.uiMessages.collectAsStateWithLifecycle()
    val dialog by viewModel.dialog.collectAsStateWithLifecycle()
    val selectedMessages by viewModel.selectedMessages.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()
    val scrollIndex by viewModel.isNeedToScrollToIndex.collectAsStateWithLifecycle()
    val showKeyboard by viewModel.showKeyboard.collectAsStateWithLifecycle()
    val voicePlayback by viewModel.voicePlayback.collectAsStateWithLifecycle()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val voiceRecordingDurationSec by viewModel.voiceRecordingDurationSec.collectAsStateWithLifecycle()
    val isRecordingVideoMessage by viewModel.isRecordingVideoMessage.collectAsStateWithLifecycle()
    val stickerPacks by viewModel.stickerPacks.collectAsStateWithLifecycle()
    val isStickerPickerOpen by viewModel.isStickerPickerOpen.collectAsStateWithLifecycle()
    val isAttachmentPickerOpen by viewModel.isAttachmentPickerOpen.collectAsStateWithLifecycle()
    val pickPhotoRequest by viewModel.pickPhotoRequest.collectAsStateWithLifecycle()

    val recordAudioPermissionState =
        rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA)

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onSendPhoto(uri)
        }
    }

    LaunchedEffect(pickPhotoRequest) {
        if (pickPhotoRequest > 0) {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    var pendingRecordStart by remember { mutableStateOf(false) }

    LaunchedEffect(recordAudioPermissionState.status) {
        if (recordAudioPermissionState.status.isGranted && pendingRecordStart) {
            pendingRecordStart = false
            viewModel.onActionButtonClicked()
        }
    }

    LaunchedEffect(navigationEvent) {
        val needToConsume = when (val navigation = navigationEvent) {
            null -> false

            is MessageNavigation.ChatMaterials -> {
                val (peerId, cmId) = navigation
                onNavigateToChatMaterials(peerId, cmId)
                true
            }

            is MessageNavigation.Profile -> {
                onNavigateToProfile(navigation.userId)
                true
            }
        }
        if (needToConsume) viewModel.onNavigationConsumed()
    }

    MessagesHistoryScreen(
        screenState = screenState,
        messages = messages.toImmutableList(),
        uiMessages = uiMessages.toImmutableList(),
        isSelectedAtLeastOne = selectedMessages.isNotEmpty(),
        scrollIndex = scrollIndex,
        selectedMessages = selectedMessages.toImmutableList(),
        baseError = baseError,
        canPaginate = canPaginate,
        showEmojiButton = AppSettings.General.showEmojiButton,
        showAttachmentButton = AppSettings.General.showAttachmentButton,
        showKeyboard = showKeyboard,
        voicePlayback = voicePlayback,
        isRecordingVoice = isRecordingVoice,
        voiceRecordingDurationSec = voiceRecordingDurationSec,
        onPlayVoiceMessageClicked = viewModel::onVoiceMessageClicked,
        onMessageSeen = viewModel::onMessageSeen,
        onBack = onBack,
        onClose = viewModel::onCloseButtonClicked,
        onScrolledToIndex = viewModel::onScrolledToIndex,
        onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
        onTopBarClicked = viewModel::onTopBarClicked,
        onRefresh = viewModel::onRefresh,
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onMessageInputChanged = viewModel::onMessageInputChanged,
        onAttachmentButtonClicked = viewModel::onAttachmentButtonClicked,
        onActionButtonClicked = viewModel::onActionButtonClicked,
        onRecordStart = {
            if (screenState.actionMode == ActionMode.RECORD_VIDEO) {
                if (!recordAudioPermissionState.status.isGranted) {
                    recordAudioPermissionState.launchPermissionRequest()
                } else if (!cameraPermissionState.status.isGranted) {
                    cameraPermissionState.launchPermissionRequest()
                } else {
                    viewModel.onRecordStart()
                }
            } else {
                if (!recordAudioPermissionState.status.isGranted) {
                    pendingRecordStart = true
                    recordAudioPermissionState.launchPermissionRequest()
                } else {
                    viewModel.onRecordStart()
                }
            }
        },
        onRecordFinish = viewModel::onRecordFinish,
        onRecordCancel = viewModel::onRecordCancel,
        onEmojiButtonClicked = viewModel::onEmojiButtonClicked,
        onEmojiButtonLongClicked = viewModel::onEmojiButtonLongClicked,
        onMessageClicked = viewModel::onMessageClicked,
        onMessageLongClicked = viewModel::onMessageLongClicked,
        onPhotoClicked = onNavigateToPhotoViewer,
        onPinnedMessageClicked = viewModel::onPinnedMessageClicked,
        onUnpinMessageButtonClicked = viewModel::onUnpinMessageClicked,
        onEditSelectedMessageClicked = viewModel::onEditSelectedMessageClicked,
        onDeleteSelectedButtonClicked = viewModel::onDeleteSelectedMessagesClicked,
        onBoldRequested = viewModel::onBoldClicked,
        onItalicRequested = viewModel::onItalicClicked,
        onUnderlineRequested = viewModel::onUnderlineClicked,
        onLinkRequested = viewModel::onLinkClicked,
        onRegularRequested = viewModel::onRegularClicked,
        onReplyCloseClicked = viewModel::onReplyCloseClicked,
        onRequestReplyToMessage = viewModel::onRequestReplyToMessage,
        onKeyboardShown = viewModel::onKeyboardShown
    )

    HandleDialogs(
        screenState = screenState,
        dialog = dialog,
        messageReadPeersLoader = viewModel::loadMessageReadPeers,
        onConfirmed = viewModel::onDialogConfirmed,
        onDismissed = viewModel::onDialogDismissed,
        onItemPicked = viewModel::onDialogItemPicked
    )

    VideoMessageRecorderDialog(
        isOpen = isRecordingVideoMessage,
        onDismiss = viewModel::onCancelVideoMessageRecord,
        onSendVideoMessage = viewModel::onSendVideoMessage
    )

    EmojiStickerPickerDialog(
        isOpen = isStickerPickerOpen,
        packs = stickerPacks,
        onDismiss = viewModel::onStickerPickerDismissed,
        onEmojiSelected = viewModel::onEmojiSelected,
        onStickerPicked = viewModel::onSendSticker
    )

    AttachmentPickerDialog(
        isOpen = isAttachmentPickerOpen,
        onDismiss = viewModel::onAttachmentPickerDismissed,
        onPickPhotoClicked = viewModel::onPickPhotoClicked
    )
}
