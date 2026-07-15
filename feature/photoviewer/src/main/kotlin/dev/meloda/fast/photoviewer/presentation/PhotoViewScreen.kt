package dev.meloda.fast.photoviewer.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.meloda.fast.photoviewer.PhotoViewViewModel
import dev.meloda.fast.photoviewer.model.PhotoViewArguments
import dev.meloda.fast.photoviewer.model.PhotoViewEffect
import dev.meloda.fast.photoviewer.model.PhotoViewIntent
import dev.meloda.fast.photoviewer.model.PhotoViewNavigationIntent
import dev.meloda.fast.photoviewer.model.PhotoViewScreenState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.FullScreenDialog
import dev.meloda.fast.ui.components.Loader
import dev.meloda.fast.ui.util.getImage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

@Composable
fun PhotoViewDialog(
    onDismiss: () -> Unit,
    arguments: PhotoViewArguments?
) {
    if (arguments != null) {
        FullScreenDialog(
            modifier = Modifier,
            onDismiss = onDismiss
        ) {
            val viewModel: PhotoViewViewModel = koinViewModel()
            val screenState by viewModel.screenStateFlow.collectAsStateWithLifecycle()

            LaunchedEffect(true) {
                viewModel.setArguments(arguments)
            }

            LaunchedEffect(Unit) {
                viewModel.screenEffectFlow.onEach { effect ->
                    when (effect) {
                        is PhotoViewEffect.Navigate -> {
                            when (effect.intent) {
                                PhotoViewNavigationIntent.Back -> onDismiss()
                            }
                        }
                    }
                }.collect()
            }

            PhotoViewScreen(
                handleIntent = viewModel::handleIntent,
                screenState = screenState
            )
        }
    }
}

@Composable
private fun PhotoViewScreen(
    handleIntent: (PhotoViewIntent) -> Unit,
    screenState: PhotoViewScreenState
) {
    val pagerState = rememberPagerState(
        pageCount = { screenState.images.size },
        initialPage = screenState.selectedPage
    )

    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { handleIntent(PhotoViewIntent.PageChange(it)) }
    }

    var offsetY by remember { mutableFloatStateOf(0f) }

    val alpha by snapshotFlow {
        if (offsetY == 0f) {
            1f
        } else {
            (windowInfo.containerSize.width.toFloat() / (abs(offsetY) * 4))
                .coerceIn(0f, 1f)
        }
    }.collectAsStateWithLifecycle(1f)

    Scaffold(
        topBar = {
            TopBar(
                onBack = { handleIntent(PhotoViewIntent.Back) },
                onShareClicked = { handleIntent(PhotoViewIntent.ShareClick) },
                onOpenInClicked = { handleIntent(PhotoViewIntent.OpenInClick) },
                onCopyClicked = { handleIntent(PhotoViewIntent.CopyClick) },
                onCopyLinkClicked = { handleIntent(PhotoViewIntent.CopyLinkClick) },
            )
        },
        containerColor = Color.Black.copy(alpha = alpha)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Pager(
                pagerState = pagerState,
                state = screenState,
                padding = padding,
                onBack = { handleIntent(PhotoViewIntent.Back) },
                onVerticalDrag = { offset -> offsetY = offset },
            )

            AnimatedVisibility(
                visible = screenState.isLoading,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = {}
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Loader(color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onShareClicked: () -> Unit,
    onOpenInClicked: () -> Unit,
    onCopyClicked: () -> Unit,
    onCopyLinkClicked: () -> Unit
) {
    var dropdownMenuShown by remember {
        mutableStateOf(false)
    }

    val hideDropDownMenu by rememberUpdatedState { dropdownMenuShown = false }

    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_round_24),
                    contentDescription = "Back button",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(
                onClick = { dropdownMenuShown = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert_round_24),
                    contentDescription = "Options",
                    tint = Color.White
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
                        hideDropDownMenu()
                        onShareClicked()
                    },
                    text = {
                        Text(text = stringResource(R.string.action_share))
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        hideDropDownMenu()
                        onOpenInClicked()
                    },
                    text = {
                        Text(text = stringResource(R.string.action_open_in))
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        hideDropDownMenu()
                        onCopyLinkClicked()
                    },
                    text = {
                        Text(text = stringResource(R.string.action_copy_link))
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        hideDropDownMenu()
                        onCopyClicked()
                    },
                    text = {
                        Text(text = stringResource(R.string.action_copy_image))
                    },
                )
            }
        }
    )
}

@Composable
private fun Pager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    state: PhotoViewScreenState,
    padding: PaddingValues,
    onBack: () -> Unit,
    onVerticalDrag: (offset: Float) -> Unit
) {
    val windowInfo = LocalWindowInfo.current

    val scope = rememberCoroutineScope()

    val offsetY = remember { Animatable(0f) }
    LaunchedEffect(offsetY.value) {
        onVerticalDrag(offsetY.value)
    }

    val imageModifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            this.translationY = offsetY.value
        }
        .draggable(
            state = rememberDraggableState { delta ->
                scope.launch {
                    offsetY.snapTo(offsetY.value + delta)
                }
            },
            orientation = Orientation.Vertical,
            onDragStopped = {
                if (abs(offsetY.value) / windowInfo.containerSize.height >= 0.25) {
                    onBack()
                } else {
                    scope.launch {
                        offsetY.animateTo(0f)
                    }
                }
            }
        )

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
                    modifier = imageModifier
                )
            } else {
                AsyncImage(
                    model = model,
                    contentDescription = "Image",
                    modifier = imageModifier,
                    placeholder = ColorPainter(Color.DarkGray),
                    error = ColorPainter(Color.Red)
                )
            }
        }
    }
}
