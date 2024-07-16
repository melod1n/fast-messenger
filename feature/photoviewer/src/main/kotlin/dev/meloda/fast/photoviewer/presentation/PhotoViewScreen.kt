package dev.meloda.fast.photoviewer.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.conena.nanokt.android.content.pxToDp
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.photoviewer.PhotoViewViewModel
import dev.meloda.fast.photoviewer.PhotoViewViewModelImpl
import dev.meloda.fast.photoviewer.model.PhotoViewScreenState
import dev.meloda.fast.ui.util.getImage
import org.koin.androidx.compose.koinViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import dev.meloda.fast.ui.R as UiR

@Composable
fun PhotoViewRoute(
    onBack: () -> Unit,
    viewModel: PhotoViewViewModel = koinViewModel<PhotoViewViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    PhotoViewScreen(
        screenState = screenState,
        onBack = onBack
    )
}

@Composable
fun PhotoViewScreen(
    screenState: PhotoViewScreenState = PhotoViewScreenState.EMPTY,
    onBack: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { screenState.images.size })

    var offsetY by remember { mutableFloatStateOf(0f) }

    val calculatedAlpha by remember(offsetY) {
        derivedStateOf {
            val absoluteOffset = abs(offsetY)

            1 - if (absoluteOffset >= 1700) {
                0.85f
            } else absoluteOffset / 2000
        }
    }

    Scaffold(
        modifier = Modifier.graphicsLayer(alpha = calculatedAlpha),
        topBar = { TopBar(onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background.copy(
            alpha = calculatedAlpha
        )
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Pager(
                pagerState = pagerState,
                state = screenState,
                padding = padding,
                onBack = onBack,
                onVerticalDrag = { offset -> offsetY = offset },
                modifier = Modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var dropdownMenuShown by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        modifier = modifier,
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back button"
                )
            }
        },
        actions = {
            IconButton(
                onClick = { dropdownMenuShown = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options"
                )
            }

            DropdownMenu(
                modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                expanded = dropdownMenuShown,
                onDismissRequest = { dropdownMenuShown = false },
                offset = DpOffset(x = (10).dp, y = (-60).dp)
            ) {
                DropdownMenuItem(
                    onClick = {
                        Toast.makeText(context, "Save clicked", Toast.LENGTH_SHORT).show()
                        dropdownMenuShown = false
                    },
                    text = { Text(text = "Save") },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun Pager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    state: PhotoViewScreenState,
    padding: PaddingValues,
    onBack: () -> Unit,
    onVerticalDrag: (offset: Float) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        val model = state.images[page].getImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (model is Painter) {
                Image(
                    painter = model,
                    contentDescription = "Image",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                var offsetY by remember { mutableFloatStateOf(0f) }

                val animatedOffset by animateFloatAsState(
                    targetValue = offsetY,
                    label = "animatedOffset"
                )
                var useAnimatedOffset by remember {
                    mutableStateOf(false)
                }

                AsyncImage(
                    model = model,
                    contentDescription = "Image",
                    modifier = Modifier
                        .graphicsLayer {
                            this.translationY = if (useAnimatedOffset) {
                                animatedOffset
                            } else offsetY
                        }
                        .draggable(
                            state = rememberDraggableState { delta ->
                                useAnimatedOffset = false
                                offsetY += delta
                                onVerticalDrag(offsetY)
                            },
                            orientation = Orientation.Vertical,
                            onDragStopped = {
                                if (abs(offsetY.pxToDp()) >= 200) {
                                    onBack()
                                } else {
                                    useAnimatedOffset = true
                                    offsetY = 0f
                                    onVerticalDrag(0f)
                                }
                            }
                        )
                        .fillMaxSize(),
                    placeholder = ColorPainter(Color.DarkGray),
                    error = ColorPainter(Color.Red)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PhotoViewScreenPreview() {
    PhotoViewScreen(
        screenState = PhotoViewScreenState(
            images = List(200) {
                UiImage.Resource(UiR.drawable.test_captcha)
            }
        )
    )
}
