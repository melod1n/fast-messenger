package com.meloda.app.fast.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.designsystem.LocalTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun BlurrableTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    listState: LazyListState?,
    hazeState: HazeState = remember { HazeState() }
) {
    val currentTheme = LocalTheme.current

    val toolbarColorAlpha by animateFloatAsState(
        targetValue = if (listState == null || !listState.canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    val toolbarContainerColor by animateColorAsState(
        targetValue =
        if (currentTheme.usingBlur || listState != null && !listState.canScrollBackward)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = toolbarContainerColor.copy(
                alpha = if (currentTheme.usingBlur) toolbarColorAlpha else 1f
            )
        ),
        modifier = modifier
            .then(
                if (currentTheme.usingBlur) {
                    Modifier.hazeChild(
                        state = hazeState,
                        style = HazeMaterials.thick()
                    )
                } else {
                    Modifier
                }
            )
            .fillMaxWidth(),
    )
}
