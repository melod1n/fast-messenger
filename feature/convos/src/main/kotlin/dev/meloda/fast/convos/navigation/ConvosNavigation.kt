package dev.meloda.fast.convos.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.meloda.fast.convos.ConvosViewModel
import dev.meloda.fast.convos.presentation.ConvosRoute
import dev.meloda.fast.model.BaseError
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
    activity: AppCompatActivity,
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (id: Long) -> Unit,
    onNavigateToCreateChat: () -> Unit,
    onScrolledToTop: () -> Unit
) {
    navigation<ConvoGraph>(
        startDestination = Convos
    ) {
        val convosViewModel: ConvosViewModel = with(activity) {
            getViewModel(qualifier = named(ConvosFilter.ALL))
        }
        composable<Convos> {
            val navController = LocalNavController.getOrThrow()

            ConvosRoute(
                viewModel = convosViewModel,
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onNavigateToCreateChat = onNavigateToCreateChat,
                onNavigateToArchive = { navController.navigate(Archive) },
                onScrolledToTop = onScrolledToTop
            )
        }
        composable<Archive> {
            val navController = LocalNavController.getOrThrow()

            ConvosRoute(
                viewModel = with(activity) {
                    getViewModel<ConvosViewModel>(
                        qualifier = named(ConvosFilter.ARCHIVE)
                    )
                },
                onBack = navController::navigateUp,
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onScrolledToTop = onScrolledToTop
            )
        }
    }
}
