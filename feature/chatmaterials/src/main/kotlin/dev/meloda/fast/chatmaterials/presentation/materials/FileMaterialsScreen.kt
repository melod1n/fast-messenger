package dev.meloda.fast.chatmaterials.presentation.materials

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import dev.chrisbanes.haze.hazeSource
import dev.meloda.fast.chatmaterials.model.ChatMaterialsScreenState
import dev.meloda.fast.chatmaterials.model.UiChatMaterial
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.ErrorView
import dev.meloda.fast.ui.components.FullScreenLoader
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileMaterialsScreen(
    modifier: Modifier = Modifier,
    canPaginate: Boolean,
    screenState: ChatMaterialsScreenState,
    baseError: BaseError?,
    padding: PaddingValues,
    onRefresh: () -> Unit,
    onSessionExpiredLogOutButtonClicked: () -> Unit,
    setCanScrollBackward: (Boolean) -> Unit,
    onPaginationConditionsMet: () -> Unit
) {
    val hazeState = LocalHazeState.current
    val currentTheme = LocalThemeConfig.current
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollBackward }
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
                LazyColumn(
                    state = listState,
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
                    item {
                        Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                    }
                    items(screenState.materials) { item ->
                        item as UiChatMaterial.File

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var errorLoading by remember {
                                mutableStateOf(false)
                            }

                            if (item.previewUrl != null && !errorLoading) {
                                Image(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .size(width = 64.dp, height = 48.dp),
                                    painter = rememberAsyncImagePainter(
                                        model = item.previewUrl,
                                        imageLoader = LocalContext.current.imageLoader,
                                        onState = {
                                            errorLoading = it is AsyncImagePainter.State.Error
                                        }
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            MaterialTheme.colorScheme
                                                .surfaceColorAtElevation(3.dp)
                                        )
                                        .size(width = 64.dp, height = 48.dp)
                                        .padding(4.dp),
                                    text = item.extension.uppercase(),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 40.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                LocalContentAlpha(alpha = ContentAlpha.medium) {
                                    Text(
                                        text = item.size,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
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
