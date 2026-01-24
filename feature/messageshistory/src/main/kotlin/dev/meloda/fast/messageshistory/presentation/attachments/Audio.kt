package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.meloda.fast.model.api.domain.VkAudioDomain
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import java.util.Locale

@Composable
fun Audio(
    modifier: Modifier = Modifier,
    item: VkAudioDomain
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .size(36.dp)
                .padding(4.dp),
            painter = painterResource(R.drawable.ic_play_arrow_fill_round_24),
            contentDescription = null,
            tint = contentColorFor(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LocalContentAlpha(alpha = ContentAlpha.medium) {
                Text(
                    text = item.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // TODO: 11-Apr-25, Danil Nikolaev: extract to ui model
        val formattedDuration by remember(item) {
            derivedStateOf {
                val duration = item.duration

                val days = duration / (24 * 3600)
                val hours = (duration % (24 * 3600)) / 3600
                val minutes = (duration % 3600) / 60
                val seconds = duration % 60

                val args = mutableListOf<Int>()
                if (days > 0) args.add(days)
                if (hours > 0) args.add(hours)
                args.add(minutes)
                args.add(seconds)

                val builder = StringBuilder()
                if (days > 0) builder.append("%02d:")
                if (hours > 0) builder.append("%02d:")
                builder.append("%d:%02d")

                builder.toString().format(Locale.getDefault(), *args.toTypedArray())
            }
        }

        Text(
            text = formattedDuration,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
