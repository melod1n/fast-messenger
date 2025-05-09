package dev.meloda.fast.messageshistory.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.messageshistory.MessagesHistoryViewModel
import dev.meloda.fast.messageshistory.MessagesHistoryViewModelImpl
import dev.meloda.fast.messageshistory.model.MessageNavigation
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MessagesHistoryRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onNavigateToChatMaterials: (peerId: Long, conversationMessageId: Long) -> Unit,
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

    val userSettings: UserSettings = koinInject()
    val showEmojiButton by userSettings.showEmojiButton.collectAsStateWithLifecycle()

    LaunchedEffect(navigationEvent) {
        val needToConsume = when (val navigation = navigationEvent) {
            null -> false

            is MessageNavigation.ChatMaterials -> {
                val (peerId, cmId) = navigation
                onNavigateToChatMaterials(peerId, cmId)
                true
            }
        }
        if (needToConsume) viewModel.onNavigationConsumed()
    }

    MessagesHistoryScreen(
        screenState = screenState,
        messages = messages.toImmutableList(),
        uiMessages = uiMessages.toImmutableList(),
        scrollIndex = scrollIndex,
        selectedMessages = selectedMessages.toImmutableList(),
        baseError = baseError,
        canPaginate = canPaginate,
        showEmojiButton = showEmojiButton,
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
        onEmojiButtonLongClicked = viewModel::onEmojiButtonLongClicked,
        onMessageClicked = viewModel::onMessageClicked,
        onMessageLongClicked = viewModel::onMessageLongClicked,
        onPinnedMessageClicked = viewModel::onPinnedMessageClicked,
        onUnpinMessageButtonClicked = viewModel::onUnpinMessageClicked,
        onDeleteSelectedButtonClicked = viewModel::onDeleteSelectedMessagesClicked,
        onBoldRequested = viewModel::onBoldClicked
    )

    HandleDialogs(
        screenState = screenState,
        dialog = dialog,
        onConfirmed = viewModel::onDialogConfirmed,
        onDismissed = viewModel::onDialogDismissed,
        onItemPicked = viewModel::onDialogItemPicked
    )
}
