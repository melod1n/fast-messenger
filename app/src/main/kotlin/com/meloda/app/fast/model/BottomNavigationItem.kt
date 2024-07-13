package com.meloda.app.fast.model

data class BottomNavigationItem(
    val titleResId: Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int,
    val route: Any,
)
