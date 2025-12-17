package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.vk.SendingStatus
import dev.meloda.fast.ui.theme.LocalThemeConfig

@Composable
fun BoxScope.DateStatus(
    modifier: Modifier = Modifier,
    dateContainerWidth: Dp,
    date: String,
    sendingStatus: SendingStatus,
    isImportant: Boolean,
    isPinned: Boolean,
    isEdited: Boolean,
    isOut: Boolean,
    isRead: Boolean
) {
    val theme = LocalThemeConfig.current

    Row(
        modifier = modifier.then(
            if (theme.enableAnimations) Modifier.animateContentSize()
            else Modifier
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isImportant) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.round_star_24),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )

        }
        if (isPinned) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_round_push_pin_24),
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(45f)
            )
        }
        if (isEdited) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.round_edit_24px),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.width(4.dp))

        if (isOut) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(
                    when (sendingStatus) {
                        SendingStatus.SENDING -> R.drawable.round_access_time_24
                        SendingStatus.SENT -> {
                            if (isRead) R.drawable.round_done_all_24
                            else R.drawable.ic_round_done_24
                        }

                        SendingStatus.FAILED -> R.drawable.round_error_outline_24
                    }
                ),
                tint = if (sendingStatus == SendingStatus.FAILED) MaterialTheme.colorScheme.error
                else LocalContentColor.current,
                contentDescription = null
            )
        }
    }
}
