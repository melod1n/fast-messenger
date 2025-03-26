package dev.meloda.fast.chatmaterials.presentation.materials

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import dev.meloda.fast.chatmaterials.model.ChatMaterialsScreenState
import dev.meloda.fast.chatmaterials.model.UiChatMaterial
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.ErrorView
import dev.meloda.fast.ui.components.FullScreenLoader
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoMaterialsScreen(
    modifier: Modifier = Modifier,
    canPaginate: Boolean,
    screenState: ChatMaterialsScreenState,
    baseError: BaseError?,
    padding: PaddingValues,
    onRefresh: () -> Unit,
    onSessionExpiredLogOutButtonClicked: () -> Unit,
    setCanScrollBackward: (Boolean) -> Unit,
    onPhotoClicked: (String) -> Unit,
    onPaginationConditionsMet: () -> Unit
) {
    val hazeState = LocalHazeState.current
    val currentTheme = LocalThemeConfig.current
    val gridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.canScrollBackward }
            .collect(setCanScrollBackward)
    }

    when {
        baseError != null -> {
            when (baseError) {
                is BaseError.SessionExpired -> {
                    ErrorView(
                        text = stringResource(R.string.session_expired),
                        buttonText = stringResource(R.string.action_log_out),
                        onButtonClick = onSessionExpiredLogOutButtonClicked
                    )
                }

                is BaseError.SimpleError -> {
                    ErrorView(
                        text = baseError.message,
                        buttonText = stringResource(R.string.try_again),
                        onButtonClick = onRefresh
                    )
                }
            }
        }

        screenState.isLoading && screenState.materials.isEmpty() -> FullScreenLoader()

        else -> {
            PullToRefreshBox(
                modifier = modifier
                    .fillMaxSize()
                    .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                    .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr)),
                state = pullToRefreshState,
                isRefreshing = screenState.isLoading,
                onRefresh = onRefresh,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = screenState.isLoading,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = padding.calculateTopPadding()),
                    )
                }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .then(
                            if (currentTheme.enableBlur) {
                                Modifier.hazeSource(state = hazeState)
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
                    items(items = screenState.materials) { item ->
                        item as UiChatMaterial.Photo
                        AsyncImage(
                            model = item.previewUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable(
                                    onClick = {
                                        onPhotoClicked(item.previewUrl)
                                    }
                                )
                        )
                    }
                    repeat(3) {
                        item {
                            Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
                        }
                    }
                }

                if (screenState.materials.isEmpty()) {
                    NoItemsView(
                        buttonText = stringResource(R.string.action_refresh),
                        onButtonClick = onRefresh
                    )
                }
            }
        }
    }
}
