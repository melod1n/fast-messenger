package dev.meloda.fast.messageshistory.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.ui.model.vk.MessageUiItem

@Composable
fun ActionMessageItem(
    item: MessageUiItem.ActionMessage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Text(
        text = item.text,
        modifier = modifier
            .padding(
                horizontal = 32.dp,
                vertical = 4.dp
            )
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (item.actionCmId != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))
            .fillMaxWidth()
            .padding(
                horizontal = 32.dp,
                vertical = 4.dp
            ),
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
fun ActionMessageItemPreview() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        ActionMessageItem(
            item = MessageUiItem.ActionMessage(
                id = 0,
                text = buildAnnotatedString {
                    append("You pinned message \"wow hello there\"")
                },
                actionCmId = null,
                cmId = 2135
            )
        )
    }
}
