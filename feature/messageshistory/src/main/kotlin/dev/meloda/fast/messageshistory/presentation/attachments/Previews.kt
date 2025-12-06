package dev.meloda.fast.messageshistory.presentation.attachments

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.FastIconButton
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList

@Composable
fun Previews(
    modifier: Modifier = Modifier,
    photos: ImmutableList<UiPreview>,
    onClick: (index: Int) -> Unit = {},
    onLongClick: (index: Int) -> Unit = {}
) {
    DynamicPreviewGrid(
        modifier = modifier,
        photos = photos,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DynamicPreviewGrid(
    photos: ImmutableList<UiPreview>,
    modifier: Modifier = Modifier,
    onClick: (index: Int) -> Unit = {},
    onLongClick: (index: Int) -> Unit = {}
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    val spacing = 2.dp
    val shape = RoundedCornerShape(8.dp)

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val spacingPx = with(LocalDensity.current) { spacing.toPx() }

        val rows = photos.chunked(3)

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            rows.forEachIndexed { index, row ->
                val aspectRatios = row.map { it.width.toFloat() / it.height }
                val totalAspect = aspectRatios.sum()

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEachIndexed { index, preview ->
                        val weight = aspectRatios[index] / totalAspect
                        val photoWidthPx = (maxWidthPx - spacingPx * (row.size - 1)) * weight
                        val height = photoWidthPx / aspectRatios[index]
                        val heightDp = with(LocalDensity.current) { height.toDp() }

                        Box(
                            modifier = Modifier
                                .height(heightDp)
                                .weight(weight)
                                .clip(shape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = preview.url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(heightDp)
                                    .clip(shape)
                                    .combinedClickable(
                                        onLongClick = { currentOnLongClick(index) },
                                        onClick = { currentOnClick(index) }
                                    )
                            )

                            if (preview.isVideo) {
                                FastIconButton(
                                    onClick = { currentOnClick(index) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        modifier = Modifier,
                                        painter = painterResource(R.drawable.round_fill_play_arrow_24px),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDynamicPhotoGrid() {
    val mockPhotos = listOf(
        UiPreview(0, "https://picsum.photos/id/1011/600/400", 600, 400, false),
        UiPreview(0, "https://picsum.photos/id/1012/500/500", 500, 500, false),
        UiPreview(0, "https://picsum.photos/id/1013/400/600", 400, 600, false),
        UiPreview(0, "https://picsum.photos/id/1014/600/600", 600, 600, false),
        UiPreview(0, "https://picsum.photos/id/1015/800/600", 800, 600, false),
        UiPreview(0, "https://picsum.photos/id/1016/700/500", 700, 500, false),
        UiPreview(0, "https://picsum.photos/id/1018/600/600", 600, 600, false),
        UiPreview(0, "https://picsum.photos/id/1020/600/800", 600, 800, false),
        UiPreview(0, "https://picsum.photos/id/1021/800/800", 800, 800, false),
        UiPreview(0, "https://picsum.photos/id/1022/500/700", 500, 700, false),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        DynamicPreviewGrid(photos = mockPhotos.take(10).toImmutableList())
    }
}
