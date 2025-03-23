package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.meloda.fast.ui.R as UiR

@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    text: String?,
    isOut: Boolean,
    date: String?,
    edited: Boolean,
    animate: Boolean,
    isRead: Boolean
) {
    val backgroundColor = if (!isOut) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val textColor = if (!isOut) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .widthIn(min = 56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp
            )
    ) {
        val minDateContainerWidth = remember(edited, isOut) {
            val mainPart = if (edited) 50.dp else 30.dp
            val readIndicatorPart = if (isOut) 14.dp else 0.dp

            mainPart + readIndicatorPart
        }

        val dateContainerWidth by animateDpAsState(
            targetValue = minDateContainerWidth,
            label = "dateContainerWidth"
        )

        if (text != null) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(2.dp)
                    .align(Alignment.Center)
                    .padding(end = 4.dp)
                    .padding(end = dateContainerWidth)
                    .padding(end = 4.dp)
                    .then(if (animate) Modifier.animateContentSize() else Modifier),
                color = textColor
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .defaultMinSize(minWidth = dateContainerWidth)
        ) {
            if (edited) {
                Icon(
                    imageVector = Icons.Rounded.Create,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = date.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(modifier = Modifier.width(4.dp))

            if (isOut) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(
                        if (isRead) UiR.drawable.round_done_all_24
                        else UiR.drawable.ic_round_done_24
                    ),
                    contentDescription = null
                )
            }
        }
    }
}
