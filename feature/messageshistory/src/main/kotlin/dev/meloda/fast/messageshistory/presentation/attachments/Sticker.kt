package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.meloda.fast.model.api.domain.VkStickerDomain

@Composable
fun Sticker(
    modifier: Modifier = Modifier,
    item: VkStickerDomain
) {
    Box(
        modifier = modifier.size(192.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = item.getUrl(
                width = 256,
                withBackground = false
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        )
    }
}
