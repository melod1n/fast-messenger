package dev.meloda.fast.messageshistory.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
                date = null,
                edited = message.isEdited,
            )

            if (message.showDate) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    modifier = Modifier.padding(end = 12.dp),
                    text = message.date,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
