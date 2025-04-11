package dev.meloda.fast.messageshistory.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.IconButton

@Composable
fun PinnedMessageContainer(
    modifier: Modifier = Modifier,
    pinnedMessage: VkMessage,
    title: String,
    summary: AnnotatedString?,
    canChangePin: Boolean,
    onPinnedMessageClicked: (Long) -> Unit = {},
    onUnpinMessageButtonClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onPinnedMessageClicked(pinnedMessage.id) }
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .rotate(45f)
                .alpha(0.5f),
            painter = painterResource(R.drawable.ic_round_push_pin_24),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            summary?.let { summary ->
                LocalContentAlpha(alpha = ContentAlpha.medium) {
                    Text(
                        text = summary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (canChangePin) {
            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = onUnpinMessageButtonClicked) {
                Icon(
                    modifier = Modifier.alpha(0.5f),
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
