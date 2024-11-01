package dev.meloda.fast.chatmaterials.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import dev.meloda.fast.chatmaterials.model.UiChatMaterial

@Composable
fun ChatMaterialItem(
    item: UiChatMaterial,
    onClick: () -> Unit
) {
    when (item) {
        is UiChatMaterial.Photo -> {
            AsyncImage(
                model = item.previewUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable(onClick = onClick)
            )
        }

        is UiChatMaterial.Video -> {
            AsyncImage(
                model = item.previewUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }

        is UiChatMaterial.Audio -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(text = item.artist)
                }

                Text(text = item.duration)
            }
        }

        is UiChatMaterial.File -> {}

        is UiChatMaterial.Link -> {}
    }
}
