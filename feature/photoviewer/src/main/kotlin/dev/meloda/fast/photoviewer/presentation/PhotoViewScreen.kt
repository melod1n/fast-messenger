package dev.meloda.fast.photoviewer.presentation

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import dev.meloda.fast.ui.R as UiR

@Composable
fun PhotoViewRoute(
    onBack: () -> Unit,
    viewModel: PhotoViewViewModel = koinViewModel<PhotoViewViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val shareRequest by viewModel.shareRequest.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(shareRequest) {
        if (shareRequest != null) {
            viewModel.onImageShared()

            val intent = Intent(Intent.ACTION_SEND).apply {
                setType("image/png")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, shareRequest)
            }

            val chooserIntent = Intent.createChooser(intent, null)
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                e.printStackTrace()

                scope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        UiR.string.error_occurred,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    PhotoViewScreen(
        screenState = screenState,
        onBack = onBack,
        onPageChanged = viewModel::onPageChanged,
        onShareClicked = viewModel::onShareClicked,
        onOpenInClicked = viewModel::onOpenInClicked,
        onCopyLinkClicked = viewModel::onCopyLinkClicked,
        onCopyClicked = viewModel::onCopyClicked
    )
}

@Composable
fun PhotoViewScreen(
    screenState: PhotoViewScreenState = PhotoViewScreenState.EMPTY,
    onBack: () -> Unit = {},
    onPageChanged: (index: Int) -> Unit = {},
    onShareClicked: () -> Unit = {},
    onOpenInClicked: () -> Unit = {},
    onCopyLinkClicked: () -> Unit = {},
    onCopyClicked: () -> Unit = {}
) {
    val pagerState = rememberPagerState(
        pageCount = { screenState.images.size },
        initialPage = screenState.selectedPage
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect(onPageChanged)
    }

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
        topBar = {
            TopBar(
                onBack = onBack,
                onShareClicked = onShareClicked,
                onOpenInClicked = onOpenInClicked,
                onCopyClicked = onCopyClicked,
                onCopyLinkClicked = onCopyLinkClicked,
            )
        },
        containerColor = MaterialTheme.colorScheme.background.copy(
            alpha = calculatedAlpha
        )
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                        hideDropDownMenu()
                        onShareClicked()
                    },
                    text = {
                        Text(text = stringResource(UiR.string.action_share))
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        hideDropDownMenu()
                        onOpenInClicked()
                    },
                    text = {
                        Text(text = stringResource(UiR.string.action_open_in))
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        hideDropDownMenu()
                        onCopyLinkClicked()
                    },
                    text = {
                        Text(text = stringResource(UiR.string.action_copy_link))
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        hideDropDownMenu()
                        onCopyClicked()
                    },
                    text = {
                        Text(text = stringResource(UiR.string.action_copy_image))
                    },
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
            },
            selectedPage = 0
        )
    )
}
