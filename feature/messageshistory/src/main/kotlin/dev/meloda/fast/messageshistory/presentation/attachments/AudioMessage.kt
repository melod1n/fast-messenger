package dev.meloda.fast.messageshistory.presentation.attachments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import dev.meloda.fast.common.ImmutableList
import dev.meloda.fast.ui.R
import java.util.Locale

@Composable
fun AudioMessage(
    waveform: ImmutableList<WaveForm>,
    isPlaying: Boolean,
    durationSec: Int,
    currentProgress: Float = 0f,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    strokeWidth: Dp = 2.5.dp,
    spacer: Dp = 1.5.dp,
    strokeColor: Color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.45f),
    playedStrokeColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current

    // Fallback waveform if empty
    val effectiveWaveform = if (waveform.isEmpty()) {
        val playedCount = (FALLBACK_BARS.size * currentProgress).toInt()
        FALLBACK_BARS.mapIndexed { index, value ->
            WaveForm(value = value, played = index < playedCount)
        }
    } else {
        waveform.toList()
    }

    val maxWaveVal = (effectiveWaveform.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .widthIn(min = 220.dp, max = 290.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Large Play / Pause button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onPlayClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isPlaying) R.drawable.ic_pause_round_24
                    else R.drawable.ic_play_arrow_fill_round_24
                ),
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Waveform canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                val canvasHeight = size.height
                val barWidthPx = strokeWidth.toPx()
                val spacerPx = spacer.toPx()
                val barTotalWidth = barWidthPx + spacerPx

                val maxBarsFitting = (size.width / barTotalWidth).toInt().coerceAtLeast(1)
                val barsToDraw = if (effectiveWaveform.size > maxBarsFitting) {
                    effectiveWaveform.take(maxBarsFitting)
                } else {
                    effectiveWaveform
                }

                barsToDraw.forEachIndexed { index, form ->
                    val x = index * barTotalWidth + (barWidthPx / 2)
                    // Scale value from [0..maxWaveVal] to [minBarHeight..canvasHeight]
                    val normalizedHeight = (form.value.toFloat() / maxWaveVal) * (canvasHeight - 4.dp.toPx()) + 4.dp.toPx()
                    val halfHeight = normalizedHeight / 2

                    drawLine(
                        color = if (form.played) playedStrokeColor else strokeColor,
                        start = Offset(x = x, y = (canvasHeight / 2) - halfHeight),
                        end = Offset(x = x, y = (canvasHeight / 2) + halfHeight),
                        strokeWidth = barWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Duration text
            val currentSeconds = (durationSec * currentProgress).toInt().coerceIn(0, durationSec)
            val durationText = if (isPlaying || currentProgress > 0f) {
                "${formatDuration(currentSeconds)} / ${formatDuration(durationSec)}"
            } else {
                formatDuration(durationSec)
            }

            Text(
                text = durationText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
}

data class WaveForm(
    val value: Int,
    val played: Boolean = false
)

private val FALLBACK_BARS = listOf(
    6, 12, 18, 24, 16, 10, 8, 14, 22, 28, 20, 15, 12, 18, 25, 30, 22, 14, 10,
    16, 24, 28, 18, 12, 8, 14, 20, 26, 18, 12, 10, 16, 22, 26, 18, 12
)
