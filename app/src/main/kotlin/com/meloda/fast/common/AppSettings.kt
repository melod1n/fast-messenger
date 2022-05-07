package com.meloda.fast.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


object AppSettings {

    val keyIsMultilineEnabled = booleanPreferencesKey("multiline_enabled")
    val keyUseNavigationDrawer = booleanPreferencesKey("use_nav_drawer")

}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    corruptionHandler = null,
    scope = CoroutineScope(Dispatchers.IO + Job())
)

