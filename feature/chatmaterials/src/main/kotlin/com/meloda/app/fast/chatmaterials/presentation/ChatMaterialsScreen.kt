package com.meloda.app.fast.chatmaterials.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import com.meloda.app.fast.chatmaterials.ChatMaterialsViewModel
import com.meloda.app.fast.chatmaterials.ChatMaterialsViewModelImpl
import com.meloda.app.fast.chatmaterials.model.ChatMaterialsScreenState
import com.meloda.app.fast.designsystem.LocalTheme
import com.meloda.app.fast.designsystem.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatMaterialsRoute(
    onBack: () -> Unit,
    viewModel: ChatMaterialsViewModel = koinViewModel<ChatMaterialsViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    ChatMaterialsScreen(
        screenState = screenState,
        onBack = onBack,
        onTypeChanged = viewModel::onTypeChanged,
        onRefreshDropdownItemClicked = viewModel::onRefresh,
        onRefresh = viewModel::onRefresh
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun ChatMaterialsScreen(
    screenState: ChatMaterialsScreenState,
    onBack: () -> Unit = {},
    onTypeChanged: (String) -> Unit = {},
    onRefreshDropdownItemClicked: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val currentTheme = LocalTheme.current

    val attachments = screenState.materials

    val imageLoader = LocalContext.current.imageLoader

    var moreClearBlur by rememberSaveable {
        mutableStateOf(false)
    }

    val hazeState = remember { HazeState() }
    val hazeStyle = if (moreClearBlur) HazeMaterials.ultraThin() else HazeMaterials.regular()

    var dropDownMenuExpanded by remember {
        mutableStateOf(false)
    }
    var checkedTypeIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    LaunchedEffect(checkedTypeIndex) {
        onTypeChanged(
            when (checkedTypeIndex) {
                0 -> "photo"
                1 -> "video"
                2 -> "audio"
                3 -> "doc"
                4 -> "link"
                else -> ""
            }
        )
    }

    val titles = listOf("Photos", "Videos", "Audios", "Files", "Links")

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val canScrollBackward = when (checkedTypeIndex) {
        in 0..1 -> gridState.canScrollBackward
        else -> listState.canScrollBackward
    }

    Log.d("ChatMaterialsScreen", "ChatMaterialsScreen: canScrollBackward: $canScrollBackward")

    val topBarContainerColorAlpha by animateFloatAsState(
        targetValue = if (!currentTheme.usingBlur || !canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val topBarContainerColor by animateColorAsState(
        targetValue =
        if (currentTheme.usingBlur || !canScrollBackward)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val pullToRefreshAlpha by animateFloatAsState(
        targetValue = if (!canScrollBackward) 1f else 0f,
        label = "pullToRefreshAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .then(
                        if (currentTheme.usingBlur) {
                            Modifier.hazeChild(
                                state = hazeState,
                                style = hazeStyle
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(topBarContainerColor.copy(alpha = topBarContainerColorAlpha))
                    .fillMaxWidth()
            ) {
                TopAppBar(
                    title = { Text(text = "Chat Materials") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                dropDownMenuExpanded = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Options button"
                            )
                        }

                        DropdownMenu(
                            modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                            expanded = dropDownMenuExpanded,
                            onDismissRequest = {
                                dropDownMenuExpanded = false
                            },
                            offset = DpOffset(x = (-4).dp, y = (-60).dp)
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onRefreshDropdownItemClicked()
                                    dropDownMenuExpanded = false
                                },
                                text = {
                                    Text(text = stringResource(id = R.string.action_refresh))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null
                                    )
                                }
                            )

                            if (currentTheme.usingBlur) {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = if (moreClearBlur) "Default blur" else "Clearer blur")
                                    },
                                    onClick = {
                                        moreClearBlur = !moreClearBlur
                                        dropDownMenuExpanded = false
                                    }
                                )
                            }

                            HorizontalDivider()

                            titles.forEachIndexed { index, title ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        RadioButton(
                                            selected = checkedTypeIndex == index,
                                            onClick = null
                                        )
                                    },
                                    text = {
                                        Text(text = title)
                                    },
                                    onClick = {
                                        checkedTypeIndex = index
                                        dropDownMenuExpanded = false
                                    }
                                )
                            }

                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            if (checkedTypeIndex in listOf(0, 1)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .then(
                            if (currentTheme.usingBlur) {
                                Modifier.haze(
                                    state = hazeState,
                                    style = hazeStyle
                                )
                            } else {
                                Modifier
                            }
                        )
                        .fillMaxSize()

                ) {
                    repeat(3) {
                        item {
                            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                        }
                    }
                    items(attachments) { item ->
                        ChatMaterialItem(
                            item = item,
                            imageLoader = imageLoader
                        )
                    }
                    repeat(3) {
                        item {
                            Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .then(
                            if (currentTheme.usingBlur) {
                                Modifier.haze(
                                    state = hazeState,
                                    style = hazeStyle
                                )
                            } else {
                                Modifier
                            }
                        )
                        .fillMaxSize()

                ) {
                    item {
                        Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                    }
                    items(attachments) { item ->
                        ChatMaterialItem(
                            item = item,
                            imageLoader = imageLoader
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
                    }
                }
            }

            if (pullToRefreshState.isRefreshing) {
                LaunchedEffect(true) {
                    onRefresh()
                }
            }

            LaunchedEffect(screenState.isLoading) {
                if (!screenState.isLoading) {
                    pullToRefreshState.endRefresh()
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier
                    .alpha(pullToRefreshAlpha)
                    .align(Alignment.TopCenter)
                    .padding(top = padding.calculateTopPadding()),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
