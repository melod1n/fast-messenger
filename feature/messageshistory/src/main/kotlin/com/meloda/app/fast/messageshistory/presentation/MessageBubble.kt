package com.meloda.app.fast.messageshistory.presentation

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
    text: String,
    isOut: Boolean,
    isTopPortion: Boolean,
    isBottomPortion: Boolean
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
            .padding(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.Center)
        )
    }
}
