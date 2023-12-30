package com.meloda.fast.screens.updates.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.ext.getString
import com.meloda.fast.screens.updates.UpdatesViewModel
import com.meloda.fast.screens.updates.UpdatesViewModelImpl
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreenContent(
    onBackClick: () -> Unit,
    viewModel: UpdatesViewModel = koinViewModel<UpdatesViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val updateState = screenState.updateState

    val downloadProgress = screenState.currentDownloadProgress
    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress / 100f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = ""
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Application updates") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    updateState.isLoading() -> CircularProgressIndicator()
                    updateState.isDownloading() -> {
                        Text(
                            text = stringResource(R.string.fragment_updates_downloading_update),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (animatedProgress > 0) {
                            LinearProgressIndicator(progress = animatedProgress)
                        } else {
                            LinearProgressIndicator()
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(onClick = viewModel::onCancelDownloadButtonClicked) {
                            Text(text = stringResource(R.string.action_stop))
                        }
                    }

                    else -> {
                        screenState.title?.getString()?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        screenState.subtitle?.getString()?.let { subtitle ->
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        screenState.updateItem?.changelog?.let {
                            Text(
                                text = stringResource(R.string.fragment_updates_changelog),
                                style = TextStyle(textDecoration = TextDecoration.Underline),
                                modifier = Modifier.clickable(onClick = viewModel::onChangelogButtonClicked)
                            )
                        }

                        screenState.actionButtonText?.getString()?.let { buttonText ->
                            Spacer(modifier = Modifier.height(24.dp))
                            ExtendedFloatingActionButton(
                                onClick = viewModel::onActionButtonClicked,
                                modifier = Modifier,
                                text = { Text(text = buttonText) },
                                icon = {
                                    screenState.actionButtonIcon?.let { resId ->
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            painter = painterResource(id = resId),
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }

                        if (updateState.isDownloaded()) {
                            Spacer(modifier = Modifier.height(48.dp))
                            Text(
                                text = stringResource(R.string.fragment_updates_issues_installing),
                                style = TextStyle(textDecoration = TextDecoration.Underline),
                                modifier = Modifier.clickable(onClick = viewModel::onIssuesButtonClicked),
                            )
                        }
                    }
                }
            }
        }
    }
}
