package com.meloda.app.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import com.meloda.app.fast.common.extensions.orDots
import com.meloda.app.fast.messageshistory.model.UiMessage

@Composable
fun IncomingMessageBubble(
    modifier: Modifier = Modifier,
    message: UiMessage,
    isTopPortion: Boolean,
    isBottomPortion: Boolean
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth(0.75f)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        if (message.isInChat) {

            val avatarBottomPadding by animateDpAsState(
                targetValue = if (message.showDate) 24.dp else 8.dp,
                label = "avatarBottomPadding"
            )
            Image(
                painter =
                message.avatar.extractUrl()?.let { url ->
                    rememberAsyncImagePainter(
                        model = url,
                        imageLoader = context.imageLoader
                    )
                } ?: painterResource(id = message.avatar.extractResId()),
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = avatarBottomPadding)
                    .size(28.dp)
                    .alpha(if (message.showAvatar) 1f else 0f)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
            AnimatedVisibility(visible = message.showName) {
                Text(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .widthIn(max = 140.dp),
                    text = message.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            MessageBubble(
                modifier = Modifier,
                text = message.text.orDots(),
                isOut = false,
                isTopPortion = isTopPortion,
                isBottomPortion = isBottomPortion,
            )

            AnimatedVisibility(visible = message.showDate) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = message.date,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

    }
}
