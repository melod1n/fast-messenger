package dev.meloda.fast.messageshistory.presentation

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

@Composable
fun OutgoingMessageBubble(
    modifier: Modifier = Modifier,
    message: UiItem.Message,
    animate: Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth(0.75f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End,
        ) {
            MessageBubble(
                modifier = Modifier,
                text = message.text.orDots(),
                isOut = true,
                date = message.date,
                edited = message.isEdited,
                animate = animate,
                isRead = message.isRead,
                sendingStatus = message.sendingStatus
            )
        }
    }
}
