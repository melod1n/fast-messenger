package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conena.nanokt.android.content.pxToDp
import dev.meloda.fast.domain.util.annotated
import dev.meloda.fast.domain.util.orEmpty
import dev.meloda.fast.ui.common.FastPreview
import dev.meloda.fast.ui.theme.AppTheme

@Composable
fun Reply(
    onClick: () -> Unit,
    shape: Shape,
    backgroundColor: Color,
    innerBackgroundColor: Color,
    titleColor: Color,
    textColor: Color,
    title: String,
    summary: AnnotatedString?,
    modifier: Modifier = Modifier
) {
    var innerContainerHeight by remember {
        mutableIntStateOf(0)
    }

    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = shape
            )
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(innerBackgroundColor),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(innerContainerHeight.dp + 12.dp)
                .background(MaterialTheme.colorScheme.primary)
        )

        Column(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .padding(end = 9.dp)
                .onGloballyPositioned { innerContainerHeight = it.size.height.pxToDp() }
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
                    lineHeight = 16.sp,
                    color = textColor
                )
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
