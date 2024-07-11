package com.meloda.app.fast.chatmaterials.presentation

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
import coil.ImageLoader
import coil.compose.AsyncImage
import com.meloda.app.fast.chatmaterials.model.UiChatMaterial

@Composable
fun ChatMaterialItem(
    item: UiChatMaterial,
    imageLoader: ImageLoader
) {
    when (item) {
        is UiChatMaterial.Photo -> {
            AsyncImage(
                model = item.previewUrl,
                contentDescription = null,
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }

        is UiChatMaterial.Video -> {
            AsyncImage(
                model = item.previewUrl,
                contentDescription = null,
                imageLoader = imageLoader,
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
