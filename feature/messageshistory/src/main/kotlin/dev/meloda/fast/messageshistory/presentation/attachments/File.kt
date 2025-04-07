package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
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
import dev.meloda.fast.model.api.domain.VkFileDomain
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha

@Composable
fun File(
    modifier: Modifier = Modifier,
    item: VkFileDomain
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var errorLoading by remember {
            mutableStateOf(false)
        }

        if (/*item.previewUrl != null && */!errorLoading) {
            Image(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(width = 64.dp, height = 48.dp),
                painter = rememberAsyncImagePainter(
                    model = null,
//                    model = item.previewUrl,
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
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme
                            .surfaceColorAtElevation(3.dp)
                    )
                    .size(width = 64.dp, height = 48.dp)
                    .padding(4.dp),
                text = item.ext.uppercase(),
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            LocalContentAlpha(alpha = ContentAlpha.medium) {
//                Text(
//                    text = item.size,
//                    style = MaterialTheme.typography.bodyMedium
//                )
            }
        }
    }
}
