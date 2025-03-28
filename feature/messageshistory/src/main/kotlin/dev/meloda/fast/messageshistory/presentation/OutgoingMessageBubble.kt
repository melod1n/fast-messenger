package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.ui.theme.LocalThemeConfig

@Composable
fun OutgoingMessageBubble(
    modifier: Modifier = Modifier,
    message: UiItem.Message,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (LocalThemeConfig.current.enableAnimations) Modifier.animateContentSize()
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End,
        ) {
            MessageBubble(
                modifier = Modifier,
                text = message.text.orDots(),
                isOut = true,
                date = message.date,
                edited = message.isEdited,
                isRead = message.isRead,
                sendingStatus = message.sendingStatus,
                pinned = message.isPinned
            )
        }
    }
}
