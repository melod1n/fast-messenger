package com.meloda.fast.auth.login.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.designsystem.LocalTheme
import com.meloda.fast.auth.login.LoginViewModel
import com.meloda.fast.auth.login.LoginViewModelImpl
import org.koin.androidx.compose.koinViewModel
import com.meloda.app.fast.designsystem.R as UiR

@Composable
fun LogoRoute(
    onNavigateToMain: () -> Unit,
    onGoNextButtonClicked: () -> Unit,
    viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()
) {
    val isNeedToOpenMain by viewModel.isNeedToOpenMain.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToOpenMain) {
        if (isNeedToOpenMain) {
            viewModel.onNavigatedToMain()
            onNavigateToMain()
        }
    }

    LogoScreen(
        onLogoLongClicked = viewModel::onLogoLongClicked,
        onGoNextButtonClicked = onGoNextButtonClicked
    )
}


// TODO: 13/07/2024, Danil Nikolaev: replace with scaffold?
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogoScreen(
    onLogoLongClicked: () -> Unit = {},
    onGoNextButtonClicked: () -> Unit = {}
) {
    val currentTheme = LocalTheme.current

    Scaffold { padding ->
        val topPadding by animateDpAsState(
            targetValue = padding.calculateTopPadding(),
            label = "topPaddingAnimation"
        )
        val bottomPadding by animateDpAsState(
            targetValue = padding.calculateBottomPadding(),
            label = "bottomPaddingAnimation"
        )

        val endPadding by animateDpAsState(
            targetValue = padding.calculateEndPadding(LayoutDirection.Ltr),
            label = "endPaddingAnimation"
        )
        val startPadding by animateDpAsState(
            targetValue = padding.calculateStartPadding(LayoutDirection.Ltr),
            label = "startPaddingAnimation"
        )

        val iconWidth = if (currentTheme.isDeviceCompact) {
            100.dp
        } else {
            134.dp
        }

        val appNameTextStyle = if (currentTheme.isDeviceCompact) {
            MaterialTheme.typography.displaySmall
        } else {
            MaterialTheme.typography.displayMedium
        }

        val bottomAdditionalPadding = if (currentTheme.isDeviceCompact) {
            10.dp
        } else {
            30.dp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = startPadding,
                    top = topPadding,
                    end = endPadding,
                    bottom = bottomPadding
                )
                .padding(top = 30.dp)
                .padding(horizontal = 30.dp)
                .padding(bottom = bottomAdditionalPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = UiR.drawable.ic_logo_big),
                    contentDescription = "Application Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .width(iconWidth)
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onLongClick = onLogoLongClicked,
                            onClick = {}
                        )
                )
                Spacer(modifier = Modifier.height(46.dp))
                Text(
                    text = stringResource(id = UiR.string.fast_messenger),
                    style = appNameTextStyle,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            FloatingActionButton(
                onClick = onGoNextButtonClicked,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .testTag("go_next_fab")
            ) {
                Icon(
                    painter = painterResource(id = UiR.drawable.ic_arrow_end),
                    contentDescription = "Go button",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
