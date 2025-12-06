package dev.meloda.fast.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FastIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .size(IconButtonTokens.StateLayerSize)
                .clip(IconButtonTokens.StateLayerShape)
                .background(color = colors.containerColor(enabled))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = ripple()
                ),
        contentAlignment = Alignment.Center
    ) {
        val contentColor = colors.contentColor(enabled)
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

internal object IconButtonTokens {
    val StateLayerShape = CircleShape
    val StateLayerSize = 40.0.dp
}

@Stable
internal fun IconButtonColors.containerColor(enabled: Boolean): Color =
    if (enabled) containerColor else disabledContainerColor

@Stable
internal fun IconButtonColors.contentColor(enabled: Boolean): Color =
    if (enabled) contentColor else disabledContentColor
