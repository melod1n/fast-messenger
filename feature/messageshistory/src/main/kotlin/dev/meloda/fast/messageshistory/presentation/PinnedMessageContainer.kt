package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.RippledClickContainer

@Composable
fun PinnedMessageContainer(
    modifier: Modifier = Modifier,
    title: String,
    summary: AnnotatedString?,
    canChangePin: Boolean,
    onPinnedMessageClicked: () -> Unit = {},
    onUnpinMessageButtonClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clickable(onClick = onPinnedMessageClicked)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_round_push_pin_24),
            contentDescription = null,
            modifier = Modifier
                .rotate(45f)
                .alpha(0.5f),
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            summary?.let { summary ->
                LocalContentAlpha(alpha = ContentAlpha.medium) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        AnimatedVisibility(canChangePin) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RippledClickContainer(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    onClick = onUnpinMessageButtonClicked
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_close_24px),
                        contentDescription = null,
                        modifier = Modifier.alpha(0.5f),
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PinnedMessageContainerPreview() {
    Surface {
        PinnedMessageContainer(
            title = "Иннокентий Панфилович",
            summary = buildAnnotatedString { append("Здравствуйте, как Ваше ничего?") },
            canChangePin = true,
            onPinnedMessageClicked = {},
            onUnpinMessageButtonClicked = {}
        )
    }
}
