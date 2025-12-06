package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.RippledClickContainer

@Composable
fun ReplyContainer(
    title: String,
    text: String?,
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
) {
    val shape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.round_reply_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(text != null) {
                Text(
                    text = text.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        RippledClickContainer(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            onClick = onCloseClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.round_close_24px),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
private fun ReplyContainerPreview() {
    Surface(
        modifier = Modifier,
    ) {
        ReplyContainer(
            onCloseClicked = {},
            title = "В ответ Ишак",
            text = "Приветствую тебя, Ишак!",
        )
    }
}
