package dev.meloda.fast.friends.presentation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.friends.FriendsViewModel
import dev.meloda.fast.friends.OnlineFriendsViewModelImpl
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.ActionInvokeDismiss
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.components.SelectionType
import dev.meloda.fast.ui.model.TabItem
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel
import dev.meloda.fast.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun FriendsRoute(
    activity: AppCompatActivity,
    friendsViewModel: FriendsViewModel,
    onError: (BaseError) -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    onMessageClicked: (userid: Long) -> Unit,
    onScrolledToTop: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentTheme = LocalThemeConfig.current
    val hazeState = LocalHazeState.current

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

    val tabItems = remember {
        listOf(
            TabItem(
                titleResId = R.string.title_friends_all,
                unselectedIconResId = null,
                selectedIconResId = null
            ),
            TabItem(
                titleResId = R.string.title_friends_online,
                unselectedIconResId = null,
                selectedIconResId = null
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = tabItems::size)

    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    var orderType: String by remember { mutableStateOf("hints") }

    var showOrderDialog by remember { mutableStateOf(false) }

    val orderPriority = stringResource(UiR.string.friends_order_priority)
    val orderName = stringResource(UiR.string.friends_order_name)
    val orderRandom = stringResource(UiR.string.friends_order_random)
    val orderMobile = stringResource(UiR.string.friends_order_mobile)
    val orderSmart = stringResource(UiR.string.friends_order_smart)

    val orderTitleItems = remember {
        ImmutableList.of(
            orderPriority,
            orderName,
            orderRandom,
            orderMobile,
            orderSmart
        )
    }

    val orderItems = remember {
        listOf("hints", "name", "random", "mobile", "smart")
    }

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    if (showOrderDialog) {
        MaterialDialog(
            onDismissRequest = { showOrderDialog = false },
            confirmText = stringResource(R.string.ok),
            confirmAction = {
                orderType = orderItems[selectedIndex]
            },
            cancelText = stringResource(R.string.cancel),
            selectionType = SelectionType.Single,
            items = orderTitleItems,
            preSelectedItems = ImmutableList.of(selectedIndex),
            onItemClick = {
                selectedIndex = it
            },
            title = stringResource(UiR.string.friends_order_by_title),
            actionInvokeDismiss = ActionInvokeDismiss.Always
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(
                modifier = Modifier
                    .then(
                        if (currentTheme.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(topBarContainerColor.copy(alpha = topBarContainerColorAlpha))
                    .fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.title_friends),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    actions = {
                        IconButton(
                            onClick = {
                                showOrderDialog = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(UiR.drawable.round_filter_list_24),
                                contentDescription = null
                            )
                        }
                    }
                )
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier,
                    containerColor = Color.Transparent
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            FriendsScreen(
                viewModel = if (index == 0) friendsViewModel else with(activity) {
                    getViewModel<OnlineFriendsViewModelImpl>()
                },
                orderType = orderType,
                padding = padding,
                tabIndex = index,
                onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
                onPhotoClicked = onPhotoClicked,
                onMessageClicked = onMessageClicked,
                setCanScrollBackward = { canScrollBackward = it },
                onScrolledToTop = onScrolledToTop
            )
        }
    }
}
