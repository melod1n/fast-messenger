package com.meloda.fast.screens.conversations

// TODO: 26.08.2023, Danil Nikolaev: rewrite

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meloda.fast.ui.AppTheme

const val numberOfDots = 3
val dotSize = 6.dp
val dotColor: Color = Color.Blue
const val delayUnit = 300
const val duration = numberOfDots * delayUnit
val spaceBetween = 2.dp

@Composable
fun DotsPulsing() {

    @Composable
    fun Dot(scale: Float) {
        Spacer(
            Modifier
                .size(dotSize)
                .scale(scale)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    @Composable
    fun animateScaleWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = delayUnit * numberOfDots
            0f at delay with LinearEasing
            1f at delay + delayUnit with LinearEasing
            0f at delay + duration
        }), label = ""
    )

    val scales = arrayListOf<State<Float>>()
    for (i in 0 until numberOfDots) {
        scales.add(animateScaleWithDelay(delay = i * delayUnit))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        scales.forEach {
            Dot(it.value)
            Spacer(Modifier.width(spaceBetween))
        }
    }
}


@Composable
fun DotsElastic() {
    val minScale = 0.6f

    @Composable
    fun Dot(scale: Float) {
        Spacer(
            Modifier
                .size(dotSize)
                .scale(scaleX = minScale, scaleY = scale)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    @Composable
    fun animateScaleWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = minScale,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = duration
            minScale at delay with LinearEasing
            1f at delay + delayUnit with LinearEasing
            minScale at delay + duration
        }), label = ""
    )

    val scales = arrayListOf<State<Float>>()
    for (i in 0 until numberOfDots) {
        scales.add(animateScaleWithDelay(delay = i * delayUnit))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        scales.forEach {
            Dot(it.value)
            Spacer(Modifier.width(spaceBetween))
        }
    }
}

@Composable
fun DotsFlashing(
    modifier: Modifier = Modifier,
    dotSize: Dp = 6.dp,
    dotColor: Color = Color.Blue,
    spaceBetween: Dp = 2.dp,
    numberOfDots: Int = 3,
) {
    val minAlpha = 0.1f

    @Composable
    fun Dot(alpha: Float) = Spacer(
        Modifier
            .size(dotSize)
            .alpha(alpha)
            .background(
                color = dotColor, shape = CircleShape
            )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "")

    @Composable
    fun animateAlphaWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = minAlpha,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = duration
            minAlpha at delay with LinearEasing
            1f at delay + delayUnit with LinearEasing
            minAlpha at delay + duration
        }), label = ""
    )

    val alphas = arrayListOf<State<Float>>()
    for (i in 0 until numberOfDots) {
        alphas.add(animateAlphaWithDelay(delay = i * delayUnit))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        alphas.forEach {
            Dot(it.value)
            Spacer(Modifier.width(spaceBetween))
        }
    }
}

@Composable
fun DotsTyping(
    modifier: Modifier = Modifier,
    dotSize: Dp = 6.dp,
    dotColor: Color = Color.Blue,
    spaceBetween: Dp = 2.dp,
    numberOfDots: Int = 3,
) {
    val maxOffset = (numberOfDots * 2).toFloat()

    @Composable
    fun Dot(offset: Float) {
        Spacer(
            Modifier
                .size(dotSize)
                .offset(y = -offset.dp)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    @Composable
    fun animateOffsetWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = duration
            0f at delay with LinearEasing
            maxOffset at delay + delayUnit with LinearEasing
            0f at delay + (duration / 2)
        }), label = ""
    )

    val offsets = arrayListOf<State<Float>>()
    for (i in 0 until numberOfDots) {
        offsets.add(animateOffsetWithDelay(delay = i * delayUnit))
    }

    Row(
        modifier = modifier.padding(top = maxOffset.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        offsets.forEach {
            Dot(it.value)
            Spacer(Modifier.width(spaceBetween))
        }
    }
}

@Composable
fun DotsCollision() {
    val maxOffset = 30f
    val delayUnit = 500

    @Composable
    fun Dot(offset: Float) {
        Spacer(
            Modifier
                .size(dotSize)
                .offset(x = offset.dp)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    val offsetLeft by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = delayUnit * 3
            0f at 0 with LinearEasing
            -maxOffset at delayUnit / 2 with LinearEasing
            0f at delayUnit
        }), label = ""
    )
    val offsetRight by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = delayUnit * 3
            0f at delayUnit with LinearEasing
            maxOffset at delayUnit + delayUnit / 2 with LinearEasing
            0f at delayUnit * 2
        }), label = ""
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = maxOffset.dp)
    ) {
        Dot(offsetLeft)
        Spacer(Modifier.width(spaceBetween))
        for (i in 0 until numberOfDots - 2) {
            Dot(0f)
            Spacer(Modifier.width(spaceBetween))
        }
        Dot(offsetRight)
    }
}


@Preview(showBackground = true)
@Composable
fun DotsPreview() = AppTheme {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxSize()
    ) {
        val spaceSize = 16.dp

        Text(
            text = "Dots pulsing", //style = MaterialTheme.typography.h5
        )
        DotsPulsing()

        Spacer(Modifier.height(spaceSize))

        Text(
            text = "Dots elastic", //style = MaterialTheme.typography.h5
        )
        DotsElastic()

        Spacer(Modifier.height(spaceSize))

        Text(
            text = "Dots flashing", //style = MaterialTheme.typography.h5
        )
        DotsFlashing()

        Spacer(Modifier.height(spaceSize))

        Text(
            text = "Dots typing", //style = MaterialTheme.typography.h5
        )
        DotsTyping()

        Spacer(Modifier.height(spaceSize))

        Text(
            text = "Dots collision", //style = MaterialTheme.typography.h5
        )
        DotsCollision()
    }
}
