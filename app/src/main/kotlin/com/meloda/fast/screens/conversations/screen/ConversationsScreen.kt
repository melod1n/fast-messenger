package com.meloda.fast.screens.conversations.screen

import android.view.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.meloda.fast.R
import com.meloda.fast.ext.isUsingBlur
import com.meloda.fast.screens.conversations.ConversationsViewModel
import com.meloda.fast.screens.conversations.ConversationsViewModelImpl
import com.meloda.fast.ui.AppTheme
import com.skydoves.cloudy.Cloudy
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel = composeViewModel<ConversationsViewModelImpl>()
) {
    AppTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val listState = rememberLazyListState()
            val firstVisibleIndex by remember {
                derivedStateOf { listState.firstVisibleItemIndex }
            }

            val appBarColorAlpha by animateFloatAsState(
                targetValue = if (listState.isScrollInProgress) 0.85f else 1f,
                animationSpec = tween(0)
            )

            val insets =
                WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)

            val listTopPadding by animateDpAsState(
                targetValue = if (firstVisibleIndex == 0) 64.dp +
                        insets.asPaddingValues().calculateTopPadding()
                else 0.dp
            )

            LazyColumn(
                state = listState,
                content = {
                    items(100) { index ->
                        Text(text = "Text #${index.inc()}")
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(top = listTopPadding)
            )

            var dropDownMenuExpanded by remember {
                mutableStateOf(false)
            }

            val actions = @Composable {
                IconButton(onClick = { dropDownMenuExpanded = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_more_vert_24),
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = { dropDownMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        onClick = {
                            dropDownMenuExpanded = false
                            viewModel.onToolbarMenuItemClicked(R.id.settings)
                        },
                        text = { Text(text = "Settings") }
                    )
                }
            }

            if (isUsingBlur()) {
                Cloudy(
                    key1 = listState.isScrollInProgress,
                    radius = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            64.dp + insets
                                .asPaddingValues()
                                .calculateTopPadding()
                        ),
                ) {}
                TopAppBar(
                    title = { Text(text = "Conversations") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                    ),
                    actions = { actions() }
                )
            } else {
                TopAppBar(
                    title = { Text(text = "Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = appBarColorAlpha)
                    ),
                    actions = { actions() }
                )
            }

        }
    }
}
