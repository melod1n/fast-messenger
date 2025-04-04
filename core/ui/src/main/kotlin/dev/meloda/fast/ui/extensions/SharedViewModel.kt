package dev.meloda.fast.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController,
    route: String? = null,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    val navGraphRoute = route ?: destination.parent?.route ?: return koinViewModel(
        qualifier = qualifier,
        parameters = parameters
    )
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return koinViewModel(
        viewModelStoreOwner = parentEntry,
        qualifier = qualifier,
        parameters = parameters
    )
}
