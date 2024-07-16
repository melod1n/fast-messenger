package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    text: String?,
    isOut: Boolean,
    date: String?,
    edited: Boolean,
) {
    Box(
        modifier = modifier
            .widthIn(min = 56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isOut) {
                    MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp
            )
    ) {
        if (text != null) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(2.dp)
                    .align(Alignment.Center)
                    .animateContentSize()
            )
        }


//        val dateContainerWidth by animateDpAsState(
//            targetValue = if (edited) 50.dp else 30.dp,
//            label = "dateContainerWidth"
//        )

//        AnimatedVisibility(
//            date != null,
//            modifier = Modifier
//                .width(dateContainerWidth)
//                .align(Alignment.BottomEnd)
//        ) {
//            Row(modifier = Modifier.fillMaxWidth()) {
//                if (edited) {
//                    Icon(
//                        imageVector = Icons.Rounded.Create,
//                        contentDescription = null,
//                        modifier = Modifier.size(14.dp)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                }
//                Text(
//                    text = date.orEmpty(),
//                    style = MaterialTheme.typography.labelSmall
//                )
//                Spacer(modifier = Modifier.width(2.dp))
//            }
//        }
    }
}
