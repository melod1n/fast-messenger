package dev.meloda.fast.conversations.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.conversations.ConversationsViewModel
import dev.meloda.fast.conversations.model.ConversationNavigation
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList

@Composable
fun ConversationsRoute(
    onBack: (() -> Unit)? = null,
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (conversationId: Long) -> Unit,
    onNavigateToCreateChat: (() -> Unit)? = null,
    onNavigateToArchive: (() -> Unit)? = null,
    onScrolledToTop: () -> Unit,
    viewModel: ConversationsViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigation.collectAsStateWithLifecycle()
    val conversations by viewModel.uiConversations.collectAsStateWithLifecycle()
    val dialog by viewModel.dialog.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

    LaunchedEffect(navigationEvent) {
        val shouldBeConsumed: Boolean = when (val navigation = navigationEvent) {
            null -> false

            is ConversationNavigation.CreateChat -> {
                onNavigateToCreateChat?.invoke()
                true
            }

            is ConversationNavigation.MessagesHistory -> {
                onNavigateToMessagesHistory(navigation.peerId)
                true
            }
        }

        if (shouldBeConsumed) viewModel.onNavigationConsumed()
    }

    ConversationsScreen(
        onBack = { onBack?.invoke() },
        screenState = screenState,
        conversations = conversations.toImmutableList(),
        baseError = baseError,
        canPaginate = canPaginate,
        onConversationItemClicked = viewModel::onConversationItemClick,
        onConversationItemLongClicked = viewModel::onConversationItemLongClick,
        onOptionClicked = viewModel::onOptionClicked,
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onRefreshDropdownItemClicked = viewModel::onRefresh,
        onRefresh = viewModel::onRefresh,
        onCreateChatButtonClicked = viewModel::onCreateChatButtonClicked,
        onArchiveActionClicked = { onNavigateToArchive?.invoke() },
        setScrollIndex = viewModel::setScrollIndex,
        setScrollOffset = viewModel::setScrollOffset,
        onConsumeReselection = onScrolledToTop,
        onErrorViewButtonClicked = {
            if (baseError in listOf(BaseError.AccountBlocked, BaseError.SessionExpired)) {
                onError(requireNotNull(baseError))
            } else {
                viewModel.onErrorButtonClicked()
            }
        }
    )

    HandleDialogs(
        screenState = screenState,
        dialog = dialog,
        onConfirmed = viewModel::onDialogConfirmed,
        onDismissed = viewModel::onDialogDismissed,
        onItemPicked = viewModel::onDialogItemPicked
    )
}
