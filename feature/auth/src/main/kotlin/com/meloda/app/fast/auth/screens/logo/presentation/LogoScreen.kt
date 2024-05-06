package com.meloda.app.fast.auth.screens.logo.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.auth.navigation.AuthGraph
import com.meloda.app.fast.auth.screens.logo.LogoViewModel
import com.meloda.app.fast.auth.screens.logo.LogoViewModelImpl
import com.meloda.app.fast.auth.screens.logo.model.UiAction
import com.meloda.app.fast.common.extensions.restartApp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.auth.destinations.LoginDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import com.meloda.app.fast.designsystem.R as UiR

typealias OnAction = (UiAction) -> Unit

@Destination<AuthGraph>(
    start = true,
    route = "logo",
)
@Composable
fun LogoScreen(
    navigator: DestinationsNavigator,
    viewModel: LogoViewModel = koinViewModel<LogoViewModelImpl>(),
) {
    val context = LocalContext.current

    LogoScreenContent(
        onAction = { action ->
            when (action) {
                UiAction.NextClicked -> navigator.navigate(LoginDestination)
                UiAction.Restart -> context.restartApp()
            }
        },
        viewModel = viewModel
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogoScreenContent(
    onAction: OnAction,
    viewModel: LogoViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.isNeedToRestart) {
        viewModel.onRestarted()
        onAction(UiAction.Restart)
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(30.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = UiR.drawable.ic_logo_big),
                    contentDescription = "Application Logo",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onLongClick = viewModel::onLogoLongClicked,
                        onClick = {}
                    )
                )
                Spacer(modifier = Modifier.height(46.dp))
                Text(
                    text = stringResource(id = UiR.string.fast_messenger),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            FloatingActionButton(
                onClick = { onAction(UiAction.NextClicked) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.align(Alignment.BottomCenter)
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
