package dev.meloda.fast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.meloda.fast.common.ImmutableList

data class SegmentedButtonItem(
    val key: String,
    val iconResId: Int
)

@Composable
fun SegmentedButtonsRow(
    items: ImmutableList<SegmentedButtonItem>,
    onClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    containerShape: CornerBasedShape = RoundedCornerShape(24.dp),
    containerColor: Color = MaterialTheme.colorScheme.background,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderSize: Dp = 1.dp,
    iconContainerWidth: Dp = 42.dp,
    iconContainerHeight: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    showDividers: Boolean = true
) {
    SegmentedButtonsRow(
        modifier = modifier.sizeIn(maxHeight = iconContainerHeight + borderSize),
        items = items.mapIndexed { index, item ->
            {
                val first = index == 0
                val last = index == items.lastIndex

                if (showDividers && !first) {
                    VerticalDivider(modifier = Modifier.padding(vertical = iconContainerHeight / 4))
                }

                SegmentedButton(
                    onClick = { onClick(index) },
                    iconResId = item.iconResId,
                    modifier = Modifier.size(
                        iconContainerWidth,
                        iconContainerHeight
                    ),
                    iconSize = iconSize,
                    shape = containerShape.copy(
                        topStart = if (!first) CornerSize(0.dp) else containerShape.topStart,
                        bottomStart = if (!first) CornerSize(0.dp) else containerShape.bottomStart,
                        topEnd = if (!last) CornerSize(0.dp) else containerShape.topEnd,
                        bottomEnd = if (!last) CornerSize(0.dp) else containerShape.bottomEnd
                    )
                )
            }
        },
        containerShape = containerShape,
        containerColor = containerColor,
        borderColor = borderColor,
        borderSize = borderSize
    )
}

@Composable
fun SegmentedButtonsRow(
    items: ImmutableList<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    containerShape: CornerBasedShape = RoundedCornerShape(24.dp),
    containerColor: Color = MaterialTheme.colorScheme.background,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderSize: Dp = 1.dp,
) {
    Row(
        modifier = modifier
            .background(containerColor, containerShape)
            .border(borderSize, borderColor, containerShape)
    ) {
        items.forEach { it.invoke() }
    }
}

@Composable
fun SegmentedButton(
    onClick: () -> Unit,
    iconResId: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    shape: Shape = CircleShape
) {
    FastIconButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            painter = painterResource(iconResId),
            contentDescription = null
        )
    }
}
