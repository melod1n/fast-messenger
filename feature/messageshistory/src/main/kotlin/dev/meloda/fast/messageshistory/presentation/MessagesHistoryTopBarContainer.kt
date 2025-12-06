package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.theme.LocalThemeConfig

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessagesHistoryTopBarContainer(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    showReplyAction: Boolean,
    topBarContainerColor: Color,
    topBarContainerColorAlpha: Float,
    isClickable: Boolean,
    isMessagesSelecting: Boolean,
    isPeerAccount: Boolean,
    avatar: UiImage,
    title: String,
    showHorizontalProgressBar: Boolean,
    showPinnedContainer: Boolean,
    pinnedMessage: VkMessage?,
    pinnedTitle: String?,
    pinnedSummary: AnnotatedString?,
    showUnpinButton: Boolean,
    onTopBarClicked: () -> Unit = {},
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    onDeleteSelectedButtonClicked: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onPinnedMessageClicked: (Long) -> Unit = {},
    onUnpinMessageButtonClicked: () -> Unit = {}
) {
    val theme = LocalThemeConfig.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(topBarContainerColor.copy(alpha = topBarContainerColorAlpha))
            .then(
                if (theme.enableBlur) {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.thick()
                    )
                } else Modifier
            )
    ) {
        MessagesHistoryTopBar(
            modifier = modifier,
            hazeState = hazeState,
            showReplyAction = showReplyAction,
            isClickable = isClickable,
            isMessagesSelecting = isMessagesSelecting,
            isPeerAccount = isPeerAccount,
            avatar = avatar,
            title = title,
            onTopBarClicked = onTopBarClicked,
            onBack = onBack,
            onClose = onClose,
            onDeleteSelectedButtonClicked = onDeleteSelectedButtonClicked,
            onRefresh = onRefresh
        )

        if (showHorizontalProgressBar) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        AnimatedVisibility(!showHorizontalProgressBar) {
            HorizontalDivider()
        }

        if (showPinnedContainer) {
            PinnedMessageContainer(
                modifier = Modifier,
                title = pinnedTitle.orDots(),
                summary = pinnedSummary,
                canChangePin = showUnpinButton,
                onPinnedMessageClicked = { onPinnedMessageClicked(pinnedMessage?.id ?: -1) },
                onUnpinMessageButtonClicked = onUnpinMessageButtonClicked
            )
            HorizontalDivider()
        }
    }
}
