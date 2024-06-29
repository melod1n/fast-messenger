package com.meloda.app.fast.settings.presentation

import android.os.PowerManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.datastore.isUsingDarkMode
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.settings.HapticType
import com.meloda.app.fast.settings.SettingsViewModel
import com.meloda.app.fast.settings.SettingsViewModelImpl
import com.meloda.app.fast.settings.model.NavigationAction
import com.meloda.app.fast.settings.model.OnSettingsChangeListener
import com.meloda.app.fast.settings.model.OnSettingsClickListener
import com.meloda.app.fast.settings.model.OnSettingsLongClickListener
import com.meloda.app.fast.settings.model.SettingsItem
import com.meloda.app.fast.settings.model.SettingsScreenState
import com.meloda.app.fast.settings.presentation.items.EditTextSettingsItem
import com.meloda.app.fast.settings.presentation.items.ListSettingsItem
import com.meloda.app.fast.settings.presentation.items.SwitchSettingsItem
import com.meloda.app.fast.settings.presentation.items.TitleSettingsItem
import com.meloda.app.fast.settings.presentation.items.TitleSummarySettingsItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.meloda.app.fast.designsystem.R as UiR

typealias OnAction = (NavigationAction) -> Unit

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun SettingsScreen(
    onError: (BaseError) -> Unit,
    onAction: OnAction,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModelImpl>()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val hapticType = screenState.useHaptics
    if (hapticType != HapticType.None) {
        view.performHapticFeedback(hapticType.getHaptic())
        viewModel.onHapticsUsed()
    }

    val userSettings: UserSettings = koinInject()

    userSettings.enableDebugSettings(screenState.showDebugOptions)

    val currentTheme by userSettings.theme.collectAsStateWithLifecycle()
    val multilineEnabled by userSettings.multiline.collectAsStateWithLifecycle()

    val settingsList = screenState.settings

    val clickListener = OnSettingsClickListener { key ->
        when (key) {
            SettingsKeys.KEY_APPEARANCE_LANGUAGE -> {
                onAction(NavigationAction.NavigateToLanguagePicker)
            }

            else -> viewModel.onSettingsItemClicked(key)
        }

    }
    val longClickListener = OnSettingsLongClickListener(viewModel::onSettingsItemLongClicked)
    val changeListener = OnSettingsChangeListener { key, newValue ->
        when (key) {
            SettingsKeys.KEY_APPEARANCE_MULTILINE -> {
                val isUsing = newValue as? Boolean ?: false
                userSettings.useMultiline(isUsing)
            }

            SettingsKeys.KEY_APPEARANCE_DARK_THEME -> {
                val newMode = newValue as? Int ?: return@OnSettingsChangeListener
                AppCompatDelegate.setDefaultNightMode(newMode)

                val isUsing = context.getSystemService<PowerManager>()?.let { manager ->
                    isUsingDarkMode(
                        context.resources,
                        manager
                    )
                } ?: false

                userSettings.useDarkThemeChanged(isUsing)
            }

            SettingsKeys.KEY_APPEARANCE_AMOLED_THEME -> {
                val isUsing = newValue as? Boolean ?: false
                userSettings.useAmoledThemeChanged(isUsing)
            }

            SettingsKeys.KEY_USE_DYNAMIC_COLORS -> {
                val isUsing = newValue as? Boolean ?: false
                userSettings.useDynamicColorsChanged(isUsing)
            }

            SettingsKeys.KEY_APPEARANCE_BLUR -> {
                val isUsing = newValue as? Boolean ?: false
                userSettings.useBlurChanged(isUsing)
            }

            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                val isUsing = newValue as? Boolean ?: false
                userSettings.setLongPollBackground(isUsing)
            }

            SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS -> {
                val isUsing = newValue as? Boolean ?: false
                userSettings.setOnline(isUsing)
            }

            else -> viewModel.onSettingsItemChanged(key, newValue)
        }
    }

    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            val title = @Composable { Text(text = stringResource(id = UiR.string.title_settings)) }
            val navigationIcon = @Composable {
                IconButton(onClick = { onAction(NavigationAction.BackClick) }) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_round_arrow_back_24),
                        contentDescription = "Back button"
                    )
                }
            }

            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = if (currentTheme.usingBlur) 0f else 1f
                    )
                ),
                modifier = Modifier
                    .then(
                        if (currentTheme.usingBlur) {
                            Modifier.hazeChild(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else {
                            Modifier
                        }
                    )
                    .fillMaxWidth()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .then(
                    if (currentTheme.usingBlur) {
                        Modifier.haze(
                            state = hazeState,
                            style = HazeMaterials.thick()
                        )
                    } else Modifier
                )
                .fillMaxWidth()
                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            items(
                count = settingsList.size,
//                key = { index ->
//                    val item = settingsList[index]
//                    requireNotNull(item.title ?: item.summary)
//                },
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
                val needToShowSpacer by remember {
                    derivedStateOf {
                        index == 0
                    }
                }

                if (needToShowSpacer) {
                    Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                }

                when (val item = settingsList[index]) {
                    is SettingsItem.Title -> TitleSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )

                    is SettingsItem.TitleSummary -> TitleSummarySettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )

                    is SettingsItem.Switch -> SwitchSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener,
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )

                    is SettingsItem.TextField -> EditTextSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener,
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )

                    is SettingsItem.ListItem -> ListSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener,
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )
                }

                val showBottomNavigationBarsSpacer by remember {
                    derivedStateOf {
                        index == settingsList.size - 1
                    }
                }

                if (showBottomNavigationBarsSpacer) {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    HandlePopups(
        performCrashPositiveClick = viewModel::onPerformCrashPositiveButtonClicked,
        performCrashDismissed = viewModel::onPerformCrashAlertDismissed,
        logoutPositiveClick = {
            viewModel.onLogOutAlertPositiveClick()
            onAction(NavigationAction.NavigateToAuth)
        },
        logoutDismissed = viewModel::onLogOutAlertDismissed,
        longPollingPositiveClick = viewModel::onLongPollingAlertPositiveClicked,
        longPollingDismissed = viewModel::onLongPollingAlertDismissed,
        screenState = screenState
    )
}

// TODO: 12/04/2024, Danil Nikolaev: rewrite to UiAction
@Composable
fun HandlePopups(
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
            text = UiText.Simple("App will be crashed. Are you sure?"),
            confirmText = UiText.Resource(UiR.string.yes),
            confirmAction = positiveClick,
            cancelText = UiText.Resource(UiR.string.cancel),
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
            if (isEasterEgg) UiR.string.easter_egg_log_out_dmitry
            else UiR.string.sign_out_confirm_title
        )

        val positiveText = UiText.Resource(
            if (isEasterEgg) UiR.string.easter_egg_log_out_dmitry
            else UiR.string.action_sign_out
        )

        MaterialDialog(
            title = title,
            text = UiText.Resource(UiR.string.sign_out_confirm),
            confirmText = positiveText,
            confirmAction = positiveClick,
            cancelText = UiText.Resource(UiR.string.cancel),
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
            title = UiText.Resource(UiR.string.warning),
            text = UiText.Simple("Long polling in background required notifications permission on Android 13 and up"),
            confirmText = UiText.Simple("Grant"),
            confirmAction = positiveClick,
            cancelText = UiText.Resource(UiR.string.cancel),
            onDismissAction = dismiss
        )
    }
}
