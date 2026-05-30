package dev.meloda.fast.convos.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.meloda.fast.convos.ConvosViewModel
import dev.meloda.fast.convos.model.ConvoNavigationIntent
import dev.meloda.fast.convos.presentation.ConvosRoute
import dev.meloda.fast.model.ConvosFilter
import dev.meloda.fast.ui.extensions.getOrThrow
import dev.meloda.fast.ui.theme.LocalNavController
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.qualifier.named

@Serializable
object ConvoGraph

@Serializable
object Convos

@Serializable
object Archive

fun NavGraphBuilder.convosGraph(
    handleNavigationIntent: (ConvoNavigationIntent) -> Unit,
    activity: AppCompatActivity,
) {
    navigation<ConvoGraph>(
        startDestination = Convos
    ) {
        composable<Convos> {
            ConvosRootRoute(
                handleNavigationIntent = handleNavigationIntent,
                viewModel = with(activity) {
                    getViewModel(named(ConvosFilter.ALL))
                }
            )
        }
        composable<Archive> {
            ConvosRootRoute(
                handleNavigationIntent = handleNavigationIntent,
                viewModel = with(activity) {
                    getViewModel<ConvosViewModel>(named(ConvosFilter.ARCHIVE))
                }
            )
        }
    }
}

@Composable
private fun ConvosRootRoute(
    handleNavigationIntent: (ConvoNavigationIntent) -> Unit,
    viewModel: ConvosViewModel
) {
    val navController = LocalNavController.getOrThrow()

    val screenState by viewModel.screenStateFlow.collectAsStateWithLifecycle()
    val navigationIntent by viewModel.navigationIntentFlow.collectAsStateWithLifecycle()

    LaunchedEffect(navigationIntent) {
        navigationIntent?.let {
            when (navigationIntent) {
                ConvoNavigationIntent.Back -> navController.navigateUp()
                ConvoNavigationIntent.Archive -> navController.navigate(Archive)
                else -> handleNavigationIntent(it)
            }

            viewModel.onNavigationConsumed()
        }
    }

    ConvosRoute(
        handleIntent = viewModel::handleIntent,
        screenState = screenState,
        isArchive = viewModel.filter == ConvosFilter.ARCHIVE,
    )
}
