package dev.meloda.fast.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.Qualifier

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController,
    qualifier: Qualifier? = null,
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel(qualifier = qualifier)
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return koinViewModel(
        viewModelStoreOwner = parentEntry,
        qualifier = qualifier
    )
}
