package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Reply(
    onClick: () -> Unit,
    bottomPadding: Dp,
    shape: Shape,
    backgroundColor: Color,
    innerBackgroundColor: Color,
    title: String,
    summary: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = shape
            )
            .height(40.dp)
            .padding(
                top = 4.dp,
                start = 4.dp,
                end = 4.dp,
                bottom = bottomPadding
            )
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .fillMaxSize()
                .background(innerBackgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onBackground)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                summary?.let {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyBasePreview(
    backgroundColor: Color,
    innerBackgroundColor: Color
) {
    Reply(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp
        ),
        onClick = {},
        title = "Danil Nikolaev",
        summary = "2 photos",
        backgroundColor = backgroundColor,
        innerBackgroundColor = innerBackgroundColor,
        bottomPadding = 0.dp
    )
}

@Preview
@Composable
private fun IncomingReplyPreview() {
    ReplyBasePreview(
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        innerBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(20.dp)
    )
}

@Preview
@Composable
private fun OutgoingReplyPreview() {
    ReplyBasePreview(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        innerBackgroundColor = MaterialTheme.colorScheme.inversePrimary
    )
}
