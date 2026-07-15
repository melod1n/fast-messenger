package dev.meloda.fast.convos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.RippledClickContainer
import dev.meloda.fast.ui.util.getImage

@Composable
fun UserChip(
    image: UiImage?,
    userName: String,
    modifier: Modifier = Modifier,
    onCloseClick: (() -> Unit)? = null,
    internalPadding: Dp = 4.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    containerShape: Shape = RoundedCornerShape(12.dp),
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Row(
        modifier = modifier
            .background(containerColor, containerShape)
            .defaultMinSize(minHeight = 38.dp),
        horizontalArrangement = Arrangement.spacedBy(internalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (image != null) {
            AsyncImage(
                model = image.getImage(),
                contentDescription = null,
                modifier = Modifier
                    .padding(
                        start = internalPadding * 2,
                        end = internalPadding
                    )
                    .size(24.dp)
                    .clip(CircleShape)
            )
        }

        Text(
            text = userName,
            color = contentColor,
            fontSize = 14.sp
        )

        if (onCloseClick != null) {
            RippledClickContainer(
                onClick = onCloseClick,
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = internalPadding)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close_round_24),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
