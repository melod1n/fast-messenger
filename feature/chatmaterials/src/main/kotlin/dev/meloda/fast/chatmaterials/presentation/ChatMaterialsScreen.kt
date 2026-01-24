package dev.meloda.fast.chatmaterials.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.chatmaterials.ChatMaterialsViewModel
import dev.meloda.fast.chatmaterials.ChatMaterialsViewModelImpl
import dev.meloda.fast.chatmaterials.model.MaterialType
import dev.meloda.fast.chatmaterials.presentation.materials.AudioMaterialsScreen
import dev.meloda.fast.chatmaterials.presentation.materials.FileMaterialsScreen
import dev.meloda.fast.chatmaterials.presentation.materials.LinkMaterialsScreen
import dev.meloda.fast.chatmaterials.presentation.materials.PhotoMaterialsScreen
import dev.meloda.fast.chatmaterials.presentation.materials.VideoMaterialsScreen
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.TabItem
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun ChatMaterialsRoute(
    onBack: () -> Unit,
    onPhotoClicked: (url: String) -> Unit,
) {

    ChatMaterialsScreen(
        onBack = onBack,
        onPhotoClicked = onPhotoClicked
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun ChatMaterialsScreen(
    onBack: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val currentTheme = LocalThemeConfig.current
    val hazeState = remember { HazeState(true) }

    val titles = remember {
        listOf(
            R.string.chat_attachment_photos,
            R.string.chat_attachment_videos,
            R.string.chat_attachment_music,
            R.string.chat_attachment_files,
            R.string.chat_attachment_links,
        )
    }

    val tabItems = remember {
        titles.map { resId ->
            TabItem(
                titleResId = resId,
                unselectedIconResId = null,
                selectedIconResId = null
            )
        }
    }

    var canScrollBackward by remember {
        mutableStateOf(false)
    }

    val topBarContainerColorAlpha by animateFloatAsState(
        targetValue = if (!currentTheme.enableBlur || !canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val topBarContainerColor by animateColorAsState(
        targetValue = if (currentTheme.enableBlur || !canScrollBackward)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val pagerState = rememberPagerState(
        pageCount = tabItems::size
    )

    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .then(
                        if (currentTheme.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else Modifier
                    )
                    .background(topBarContainerColor.copy(alpha = topBarContainerColorAlpha))
                    .fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.chat_materials_title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back_round_24),
                                contentDescription = null
                            )
                        }
                    }
                )
                PrimaryScrollableTabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp
                ) {
                    tabItems.forEachIndexed { index, item ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                item.titleResId?.let { resId ->
                                    Text(text = stringResource(id = resId))
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                when (index) {
                    0 -> {
                        val viewModel: ChatMaterialsViewModel =
                            koinViewModel<ChatMaterialsViewModelImpl>(named(MaterialType.PHOTO))
                        val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                        val baseError by viewModel.baseError.collectAsStateWithLifecycle()
                        val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

                        PhotoMaterialsScreen(
                            modifier = Modifier,
                            screenState = screenState,
                            baseError = baseError,
                            padding = padding,
                            onRefresh = viewModel::onRefresh,
                            onSessionExpiredLogOutButtonClicked = { },
                            setCanScrollBackward = { canScrollBackward = it },
                            canPaginate = canPaginate,
                            onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
                            onPhotoClicked = onPhotoClicked
                        )
                    }

                    1 -> {
                        val viewModel: ChatMaterialsViewModel =
                            koinViewModel<ChatMaterialsViewModelImpl>(named(MaterialType.VIDEO))
                        val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                        val baseError by viewModel.baseError.collectAsStateWithLifecycle()
                        val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

                        VideoMaterialsScreen(
                            modifier = Modifier,
                            screenState = screenState,
                            baseError = baseError,
                            padding = padding,
                            onRefresh = viewModel::onRefresh,
                            onSessionExpiredLogOutButtonClicked = { },
                            setCanScrollBackward = { canScrollBackward = it },
                            canPaginate = canPaginate,
                            onPaginationConditionsMet = viewModel::onPaginationConditionsMet
                        )
                    }

                    2 -> {
                        val viewModel: ChatMaterialsViewModel =
                            koinViewModel<ChatMaterialsViewModelImpl>(named(MaterialType.AUDIO))
                        val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                        val baseError by viewModel.baseError.collectAsStateWithLifecycle()
                        val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

                        AudioMaterialsScreen(
                            modifier = Modifier,
                            screenState = screenState,
                            baseError = baseError,
                            padding = padding,
                            onRefresh = viewModel::onRefresh,
                            onSessionExpiredLogOutButtonClicked = { },
                            setCanScrollBackward = { canScrollBackward = it },
                            canPaginate = canPaginate,
                            onPaginationConditionsMet = viewModel::onPaginationConditionsMet
                        )
                    }

                    3 -> {
                        val viewModel: ChatMaterialsViewModel =
                            koinViewModel<ChatMaterialsViewModelImpl>(named(MaterialType.FILE))
                        val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                        val baseError by viewModel.baseError.collectAsStateWithLifecycle()
                        val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

                        FileMaterialsScreen(
                            modifier = Modifier,
                            screenState = screenState,
                            baseError = baseError,
                            padding = padding,
                            onRefresh = viewModel::onRefresh,
                            onSessionExpiredLogOutButtonClicked = { },
                            setCanScrollBackward = { canScrollBackward = it },
                            canPaginate = canPaginate,
                            onPaginationConditionsMet = viewModel::onPaginationConditionsMet
                        )
                    }

                    4 -> {
                        val viewModel: ChatMaterialsViewModel =
                            koinViewModel<ChatMaterialsViewModelImpl>(named(MaterialType.LINK))
                        val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                        val baseError by viewModel.baseError.collectAsStateWithLifecycle()
                        val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

                        LinkMaterialsScreen(
                            modifier = Modifier,
                            screenState = screenState,
                            baseError = baseError,
                            padding = padding,
                            onRefresh = viewModel::onRefresh,
                            onSessionExpiredLogOutButtonClicked = { },
                            setCanScrollBackward = { canScrollBackward = it },
                            canPaginate = canPaginate,
                            onPaginationConditionsMet = viewModel::onPaginationConditionsMet
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}
