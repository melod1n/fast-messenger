package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import dev.meloda.fast.common.util.AndroidUtils
import dev.meloda.fast.model.api.domain.VkFileDomain
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha

@Composable
fun File(
    modifier: Modifier = Modifier,
    item: VkFileDomain,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var errorLoading by remember {
            mutableStateOf(false)
        }

        // TODO: 11-Apr-25, Danil Nikolaev: extract to ui model
        val preview by remember(item) {
            derivedStateOf {
                when (val preview = item.preview) {
                    null -> null

                    else -> {
                        when {
                            preview.photo != null -> {
                                val size = preview.photo?.sizes?.maxByOrNull { it.width }
                                size?.src
                            }

                            preview.video != null -> {
                                val size = preview.video?.src
                                size
                            }

                            else -> null
                        }
                    }
                }
            }
        }
        val formattedSize by remember(item) {
            derivedStateOf {
                AndroidUtils.bytesToHumanReadableSize(item.size.toDouble())
            }
        }

        if (preview != null && !errorLoading) {
            Image(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(width = 48.dp, height = 36.dp),
                painter = rememberAsyncImagePainter(
                    model = preview,
                    imageLoader = LocalContext.current.imageLoader,
                    onState = {
                        errorLoading = it is AsyncImagePainter.State.Error
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                    .size(width = 48.dp, height = 36.dp),
                text = item.ext.uppercase(),
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LocalContentAlpha(alpha = ContentAlpha.medium) {
                Text(
                    text = formattedSize,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
