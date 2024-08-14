package dev.meloda.fast.auth.login.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.auth.BuildConfig
import dev.meloda.fast.ui.components.ActionInvokeDismiss
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.theme.LocalSizeConfig
import org.koin.androidx.compose.koinViewModel
import dev.meloda.fast.ui.R as UiR

@Composable
fun LogoRoute(
    onNavigateToMain: () -> Unit,
    onGoNextButtonClicked: () -> Unit,
    viewModel: dev.meloda.fast.auth.login.LoginViewModel = koinViewModel<dev.meloda.fast.auth.login.LoginViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isNeedToOpenMain by viewModel.isNeedToOpenMain.collectAsStateWithLifecycle()
    val isNeedToShowSignInAlert by viewModel.isNeedToShowFastSignInAlert.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToOpenMain) {
        if (isNeedToOpenMain) {
            viewModel.onNavigatedToMain()
            onNavigateToMain()
        }
    }

    LogoScreen(
        isLoading = screenState.isLoading,
        onLogoLongClicked = viewModel::onLogoLongClicked,
        onGoNextButtonClicked = onGoNextButtonClicked
    )

    if (isNeedToShowSignInAlert) {
        SignInAlert(
            onDismissRequest = viewModel::onFastLogInAlertDismissed,
            onConfirmClick = viewModel::onFastLogInAlertConfirmClicked,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogoScreen(
    isLoading: Boolean = false,
    onLogoLongClicked: () -> Unit = {},
    onGoNextButtonClicked: () -> Unit = {}
) {
    val currentSize = LocalSizeConfig.current

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

        val iconWidth = if (currentSize.isWidthSmall) {
            110.dp
        } else {
            134.dp
        }

        val appNameTextStyle = if (currentSize.isWidthSmall) {
            MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp)
        } else {
            MaterialTheme.typography.displayMedium
        }

        val bottomAdditionalPadding = if (currentSize.isHeightSmall) {
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

            AnimatedVisibility(
                visible = !isLoading,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isLoading) {
                            onGoNextButtonClicked()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("go_next_fab")
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_arrow_end),
                        contentDescription = "Go button",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            AnimatedVisibility(
                visible = isLoading,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun SignInAlert(
    onDismissRequest: () -> Unit,
    onConfirmClick: (token: String) -> Unit
) {
    var tokenText by rememberSaveable {
        mutableStateOf("")
    }

    val maxWidthModifier = Modifier.fillMaxWidth()

    MaterialDialog(
        onDismissRequest = onDismissRequest,
        title = "Fast authorization",
        confirmText = stringResource(id = UiR.string.action_authorize),
        confirmAction = { onConfirmClick(tokenText) },
        cancelText = stringResource(id = UiR.string.cancel),
        actionInvokeDismiss = ActionInvokeDismiss.Always
    ) {
        Column(modifier = maxWidthModifier) {
            OutlinedTextField(
                modifier = maxWidthModifier.padding(horizontal = 16.dp),
                value = tokenText,
                onValueChange = { tokenText = it },
                placeholder = { Text(text = "Access token") },
                label = { Text(text = "Access token") }
            )
        }
    }
}
