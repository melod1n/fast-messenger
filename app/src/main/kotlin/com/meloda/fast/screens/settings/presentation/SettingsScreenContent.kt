package com.meloda.fast.screens.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.RequestNotificationsPermission
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.notNull
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.settings.HapticType
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.screens.settings.SettingsViewModel
import com.meloda.fast.screens.settings.SettingsViewModelImpl
import com.meloda.fast.screens.settings.UserSettings
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
import org.koin.compose.koinInject

@Composable
fun SettingsRoute(
    navigateToUpdates: () -> Unit,
    navigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    onUseDarkThemeChanged: (Boolean) -> Unit,
    onUseDynamicColorsChanged: (Boolean) -> Unit,
    onUseMultilineChanged: (Boolean) -> Unit,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModelImpl>()
) {
    val view = LocalView.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val hapticType = screenState.useHaptics
    if (hapticType != HapticType.None) {
        view.performHapticFeedback(hapticType.getHaptic())
        viewModel.onHapticsUsed()
    }

    if (screenState.isNeedToOpenUpdates) {
        viewModel.onNavigatedToUpdates()
        navigateToUpdates()
    }

    if (screenState.isNeedToRequestNotificationPermission) {
        viewModel.onNotificationsPermissionRequested()
        RequestNotificationsPermission {
            Text("Blya pizda")
        }
    }

    SettingsScreenContent(
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
                    val newMode = newValue as? Int ?: return@SettingsScreenContent
                    AppCompatDelegate.setDefaultNightMode(newMode)

                    val isUsing = isUsingDarkTheme()
                    onUseDarkThemeChanged(isUsing)
                }

                SettingsKeys.KEY_APPEARANCE_MULTILINE -> {
                    val isUsing = newValue as? Boolean ?: false
                    onUseMultilineChanged(isUsing)
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
        longPollingPositiveClick = viewModel::onLongPollingAlertPositiveClicked,
        longPollingDismissed = viewModel::onLongPollingAlertDismissed,
        screenState = screenState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onBackClick: () -> Unit,
    onSettingsItemClicked: (key: String) -> Unit,
    onSettingsItemLongClicked: (key: String) -> Unit,
    onSettingsItemChanged: (key: String, newValue: Any?) -> Unit,
    screenState: SettingsScreenState
) {
    val settings: UserSettings = koinInject()

    // TODO: 01/12/2023, Danil Nikolaev: fix
    val multilineEnabled by settings.multiline.collectAsStateWithLifecycle()

    val settingsList = screenState.settings

    val clickListener = OnSettingsClickListener(onSettingsItemClicked)
    val longClickListener = OnSettingsLongClickListener(onSettingsItemLongClicked)
    val changeListener = OnSettingsChangeListener(onSettingsItemChanged)

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
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

            LargeTopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            items(
                count = settingsList.size,
                key = { index ->
                    val item = settingsList[index]
                    (item.title ?: item.summary).notNull()
                },
                contentType = { index ->
                    when (settingsList[index]) {
                        is SettingsItem.ListItem -> "listitem"
                        is SettingsItem.Switch -> "switch"
                        is SettingsItem.TextField -> "textfield"
                        is SettingsItem.Title -> "title"
                        is SettingsItem.TitleSummary -> "titlesummary"
                    }
                }
            ) { index ->
                when (val item = settingsList[index]) {
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
    longPollingPositiveClick: () -> Unit,
    longPollingDismissed: () -> Unit,
    screenState: SettingsScreenState
) {
    val showOptions = screenState.showOptions

    PerformCrashDialog(
        positiveClick = performCrashPositiveClick,
        dismiss = performCrashDismissed,
        show = showOptions.showPerformCrash
    )

    LogOutDialog(
        positiveClick = logoutPositiveClick,
        dismiss = logoutDismissed,
        show = showOptions.showLogOut
    )

    LongPollingNotificationsPermission(
        positiveClick = longPollingPositiveClick,
        dismiss = longPollingDismissed,
        show = showOptions.showLongPollNotifications
    )
}

@Composable
fun PerformCrashDialog(
    positiveClick: () -> Unit,
    dismiss: () -> Unit,
    show: Boolean,
) {
    if (show) {
        MaterialDialog(
            title = UiText.Simple("Perform Crash"),
            message = UiText.Simple("App will be crashed. Are you sure?"),
            positiveText = UiText.Resource(R.string.yes),
            positiveAction = positiveClick,
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = dismiss
        )
    }
}

@Composable
fun LogOutDialog(
    positiveClick: () -> Unit,
    dismiss: () -> Unit,
    show: Boolean
) {
    if (show) {
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
            positiveAction = positiveClick,
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = dismiss
        )
    }
}

@Composable
fun LongPollingNotificationsPermission(
    positiveClick: () -> Unit,
    dismiss: () -> Unit,
    show: Boolean
) {
    if (show) {
        MaterialDialog(
            title = UiText.Resource(R.string.warning),
            message = UiText.Simple("Long polling in background required notifications permission on Android 13 and up"),
            positiveText = UiText.Simple("Grant"),
            positiveAction = positiveClick,
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = dismiss
        )
    }
}
