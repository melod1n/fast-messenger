package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.FastIconButton
import kotlin.collections.forEachIndexed

@Composable
fun AudioMessage(
    waveform: List<WaveForm>,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(25),
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    strokeWidth: Dp = 2.dp,
    spacer: Dp = 1.dp,
    strokeColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val density = LocalDensity.current
    val requiredWidthDp = waveform.size * (2.dp + 1.dp) + 1.dp

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .height(50.dp)
            .widthIn(min = requiredWidthDp + 20.dp + 36.dp + 2.dp)
            .padding(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .padding(start = 36.dp)
                .padding(start = 2.dp)
                .matchParentSize()
                .align(Alignment.Center)
        ) {
            val width = size.width
            val height = size.height

            waveform.forEachIndexed { index, form ->
                val start = with(density) {
                    Offset(
                        x = index * (strokeWidth.toPx() + spacer.toPx()) + spacer.toPx(),
                        y = height / 2 + form.value * 0.5f
                    )
                }
                val end = Offset(x = start.x, y = height / 2 - form.value * 0.5f)

                drawLine(
                    color = strokeColor,
                    start = start,
                    end = end,
                    strokeWidth = with(density) { strokeWidth.toPx() },
                    cap = StrokeCap.Round
                )
            }
        }

        FastIconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.inversePrimary)
                .size(30.dp, 30.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(
                    if (isPlaying) R.drawable.round_pause_24
                    else R.drawable.round_fill_play_arrow_24px,
                ),
                contentDescription = null
            )
        }
    }
}

data class WaveFormState(
    val lines: List<WaveForm>,
    val isPlaying: Boolean = false
)

data class WaveForm(
    val value: Int,
    val played: Boolean = false
)
