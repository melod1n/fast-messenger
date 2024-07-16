package dev.meloda.fast.common.extensions.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.navigation.koinNavViewModel

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinNavViewModel(viewModelStoreOwner = parentEntry)
}
