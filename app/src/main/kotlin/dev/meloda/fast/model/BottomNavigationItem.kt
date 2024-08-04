package dev.meloda.fast.model

import androidx.compose.runtime.Immutable

@Immutable
data class BottomNavigationItem(
    val titleResId: Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int,
    val route: Any,
)
