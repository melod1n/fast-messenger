package com.meloda.fast.screens.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.isSystemUsingDarkMode
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.notNull
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.settings.HapticType
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.screens.settings.SettingsViewModel
import com.meloda.fast.screens.settings.SettingsViewModelImpl
import com.meloda.fast.screens.settings.model.OnSettingsChangeListener
import com.meloda.fast.screens.settings.model.OnSettingsClickListener
import com.meloda.fast.screens.settings.model.OnSettingsLongClickListener
import com.meloda.fast.screens.settings.model.SettingsItem
import com.meloda.fast.screens.settings.model.SettingsScreenState
import com.meloda.fast.screens.settings.presentation.items.EditTextSettingsItem
import com.meloda.fast.screens.settings.presentation.items.ListSettingsItem
import com.meloda.fast.screens.settings.presentation.items.SwitchSettingsItem
import com.meloda.fast.screens.settings.presentation.items.TitleSettingsItem
import com.meloda.fast.screens.settings.presentation.items.TitleSummarySettingsItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    navigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    onUseDarkThemeChanged: (Boolean) -> Unit,
    onUseDynamicColorsChanged: (Boolean) -> Unit,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModelImpl>()
) {
    val view = LocalView.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val hapticType = screenState.useHaptics
    if (hapticType != HapticType.None) {
        view.performHapticFeedback(hapticType.getHaptic())
        viewModel.onHapticsUsed()
    }

    SettingsScreen(
        onBackClick = onBackClick,
        onSettingsItemClicked = viewModel::onSettingsItemClicked,
        onSettingsItemLongClicked = viewModel::onSettingsItemLongClicked,
        onSettingsItemChanged = { key, newValue ->
            when (key) {
                SettingsKeys.KEY_USE_DYNAMIC_COLORS -> {
                    val isUsing = newValue as? Boolean ?: false
                    onUseDynamicColorsChanged(isUsing)
                }

                SettingsKeys.KEY_APPEARANCE_DARK_THEME -> {
                    val newMode = newValue as? Int ?: return@SettingsScreen
                    AppCompatDelegate.setDefaultNightMode(newMode)

                    val isUsing = isUsingDarkTheme()
                    onUseDarkThemeChanged(isUsing)
                }

                else -> viewModel.onSettingsItemChanged(key, newValue)
            }
        },
        screenState = screenState
    )

    HandleDialogs(
        performCrashPositiveClick = viewModel::onPerformCrashPositiveButtonClicked,
        performCrashDismissed = viewModel::onPerformCrashAlertDismissed,
        logoutPositiveClick = {
            viewModel.onLogOutAlertPositiveClick()
            navigateToLogin()
        },
        logoutDismissed = viewModel::onLogOutAlertDismissed,
        screenState = screenState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSettingsItemClicked: (key: String) -> Unit,
    onSettingsItemLongClicked: (key: String) -> Unit,
    onSettingsItemChanged: (key: String, newValue: Any?) -> Unit,
    screenState: SettingsScreenState
) {
    val multilineEnabled = screenState.multilineEnabled

    val settings = screenState.settings

    val clickListener = OnSettingsClickListener(onSettingsItemClicked)
    val longClickListener = OnSettingsLongClickListener(onSettingsItemLongClicked)
    val changeListener = OnSettingsChangeListener(onSettingsItemChanged)

    // TODO: 17.04.2023, Danil Nikolaev: make it work
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.systemBarsDarkContentEnabled = !isSystemUsingDarkMode()
        onDispose {}
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val title = @Composable { Text(text = "Settings") }
            val navigationIcon = @Composable {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_arrow_back_24),
                        contentDescription = null
                    )
                }
            }

            TopAppBar(
                title = title,
                navigationIcon = navigationIcon
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            items(
                count = settings.size,
                key = { index ->
                    val item = settings[index]
                    (item.title ?: item.summary).notNull()
                }
            ) { index ->
                when (val item = settings[index]) {
                    is SettingsItem.Title -> TitleSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled
                    )

                    is SettingsItem.TitleSummary -> TitleSummarySettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener
                    )

                    is SettingsItem.Switch -> SwitchSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener
                    )

                    is SettingsItem.TextField -> EditTextSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener
                    )

                    is SettingsItem.ListItem -> ListSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener
                    )
                }
            }
        }
    }
}

// TODO: 25.08.2023, Danil Nikolaev: think something of list of click & dismissed listeners
@Composable
fun HandleDialogs(
    performCrashPositiveClick: () -> Unit,
    performCrashDismissed: () -> Unit,
    logoutPositiveClick: () -> Unit,
    logoutDismissed: () -> Unit,
    screenState: SettingsScreenState
) {
    val showOptions = screenState.showOptions

    if (showOptions.showPerformCrash) {
        MaterialDialog(
            title = UiText.Simple("Perform Crash"),
            message = UiText.Simple("App will be crashed. Are you sure?"),
            positiveText = UiText.Resource(R.string.yes),
            positiveAction = performCrashPositiveClick,
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = performCrashDismissed
        )
    }

    if (showOptions.showLogOut) {
        val isEasterEgg = UserConfig.userId == SettingsKeys.ID_DMITRY

        val title = UiText.Resource(
            if (isEasterEgg) R.string.easter_egg_log_out_dmitry
            else R.string.sign_out_confirm_title
        )

        val positiveText = UiText.Resource(
            if (isEasterEgg) R.string.easter_egg_log_out_dmitry
            else R.string.action_sign_out
        )

        MaterialDialog(
            title = title,
            message = UiText.Resource(R.string.sign_out_confirm),
            positiveText = positiveText,
            positiveAction = logoutPositiveClick,
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = logoutDismissed
        )
    }
}

interface AlertDialogListener {
    fun positiveClick()
    fun negativeClick()
    fun neutralClick()
    fun dismiss()
}
