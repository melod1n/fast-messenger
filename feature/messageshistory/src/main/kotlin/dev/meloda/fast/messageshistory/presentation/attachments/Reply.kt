package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.meloda.fast.domain.util.annotated
import dev.meloda.fast.domain.util.orEmpty
import dev.meloda.fast.ui.common.FastPreview
import dev.meloda.fast.ui.theme.AppTheme

@Composable
fun Reply(
    onClick: () -> Unit,
    bottomPadding: Dp,
    shape: Shape,
    backgroundColor: Color,
    innerBackgroundColor: Color,
    titleColor: Color,
    textColor: Color,
    title: String,
    summary: AnnotatedString?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = shape
            )
            .height(48.dp)
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
                    .background(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    color = titleColor
                )

                AnimatedVisibility(summary != null) {
                    Text(
                        text = summary.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyBasePreview(
    backgroundColor: Color,
    innerBackgroundColor: Color,
    titleColor: Color,
    textColor: Color
) {
    Reply(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp
        ),
        onClick = {},
        title = "Danil Nikolaev",
        summary = "2 photos".annotated(),
        backgroundColor = backgroundColor,
        innerBackgroundColor = innerBackgroundColor,
        titleColor = titleColor,
        textColor = textColor,
        bottomPadding = 0.dp,
    )
}

@FastPreview
@Composable
private fun IncomingReplyPreview() {
    AppTheme(useDarkTheme = isSystemInDarkTheme(), useDynamicColors = true) {
        ReplyBasePreview(
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            innerBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(20.dp),
            titleColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onBackground
        )
    }
}

@FastPreview
@Composable
private fun OutgoingReplyPreview() {
    AppTheme(useDarkTheme = isSystemInDarkTheme(), useDynamicColors = true) {
        val bg = MaterialTheme.colorScheme.primaryContainer
        val inner = MaterialTheme.colorScheme.background.copy(
            if (isSystemInDarkTheme()) 0.3f else 0.6f
        )
        val title = MaterialTheme.colorScheme.primary
        val text = MaterialTheme.colorScheme.onBackground


        ReplyBasePreview(
            backgroundColor = bg,
            innerBackgroundColor = inner,
            titleColor = title,
            textColor = text
        )
    }
}
