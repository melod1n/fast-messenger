package dev.meloda.fast.convos.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.convos.ConvosViewModel
import dev.meloda.fast.convos.model.ConvoNavigation
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList

@Composable
fun ConvosRoute(
    viewModel: ConvosViewModel,
    onBack: (() -> Unit)? = null,
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (convoId: Long) -> Unit,
    onNavigateToCreateChat: (() -> Unit)? = null,
    onNavigateToArchive: (() -> Unit)? = null,
    onScrolledToTop: () -> Unit,
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigation.collectAsStateWithLifecycle()
    val convos by viewModel.uiConvos.collectAsStateWithLifecycle()
    val dialog by viewModel.dialog.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

    LaunchedEffect(navigationEvent) {
        val shouldBeConsumed: Boolean = when (val navigation = navigationEvent) {
            null -> false

            is ConvoNavigation.CreateChat -> {
                onNavigateToCreateChat?.invoke()
                true
            }

            is ConvoNavigation.MessagesHistory -> {
                onNavigateToMessagesHistory(navigation.peerId)
                true
            }
        }

        if (shouldBeConsumed) viewModel.onNavigationConsumed()
    }

    ConvosScreen(
        onBack = { onBack?.invoke() },
        screenState = screenState,
        convos = convos.toImmutableList(),
        baseError = baseError,
        canPaginate = canPaginate,
        onConvoItemClicked = viewModel::onConvoItemClick,
        onConvoItemLongClicked = viewModel::onConvoItemLongClick,
        onOptionClicked = viewModel::onOptionClicked,
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
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
