package dev.meloda.fast.messageshistory.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.emoji2.emojipicker.EmojiPickerView
import coil.compose.AsyncImage
import dev.meloda.fast.model.api.domain.VkStickerPackDomain
import dev.meloda.fast.ui.R

private enum class PickerTab { EMOJI, STICKERS }

@Composable
fun EmojiStickerPickerDialog(
    isOpen: Boolean,
    packs: List<VkStickerPackDomain>,
    onDismiss: () -> Unit,
    onEmojiSelected: (emoji: String) -> Unit,
    onStickerPicked: (stickerId: Long) -> Unit
) {
    if (!isOpen) return

    var selectedTab by remember { mutableStateOf(PickerTab.EMOJI) }
    var selectedPackId by remember { mutableStateOf<Long?>(null) }

    val activePack = packs.firstOrNull { it.id == selectedPackId } ?: packs.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .clickable(enabled = false, onClick = {}),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PickerTabButton(
                            iconResId = R.drawable.ic_sentiment_satisfied_round_24,
                            isSelected = selectedTab == PickerTab.EMOJI,
                            onClick = { selectedTab = PickerTab.EMOJI }
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        PickerTabButton(
                            iconResId = R.drawable.ic_sticker_fill_round_24,
                            isSelected = selectedTab == PickerTab.STICKERS,
                            onClick = { selectedTab = PickerTab.STICKERS }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close_round_24),
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    when (selectedTab) {
                        PickerTab.EMOJI -> {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp),
                                factory = { context ->
                                    EmojiPickerView(context).apply {
                                        setOnEmojiPickedListener { emojiViewItem ->
                                            onEmojiSelected(emojiViewItem.emoji)
                                        }
                                    }
                                }
                            )
                        }

                        PickerTab.STICKERS -> {
                            if (packs.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    items(packs, key = VkStickerPackDomain::id) { pack ->
                                        val previewId = pack.previewStickerId ?: return@items
                                        val isSelected = activePack?.id == pack.id

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                    else Color.Transparent
                                                )
                                                .clickable { selectedPackId = pack.id },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = stickerUrl(previewId, 128),
                                                contentDescription = pack.title,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val stickers = activePack?.stickerIds.orEmpty()
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = false),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(stickers) { stickerId ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { onStickerPicked(stickerId) }
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = stickerUrl(stickerId, 256),
                                                contentDescription = null,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.size(76.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerTabButton(
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun stickerUrl(stickerId: Long, width: Int): String =
    "https://vk.ru/sticker/1-$stickerId-$width"
