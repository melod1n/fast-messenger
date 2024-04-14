package com.meloda.fast.screens.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.compose.MaterialDialog
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsRoute(
    navigateToLogin: () -> Unit,
    navigateToLanguagePicker: () -> Unit,
    onBackClick: () -> Unit,
    onUseDarkThemeChanged: (Boolean) -> Unit,
    onUseAmoledThemeChanged: (Boolean) -> Unit,
    onUseDynamicColorsChanged: (Boolean) -> Unit,
    onUseBlurChanged: (Boolean) -> Unit,
    onUseMultilineChanged: (Boolean) -> Unit,
    onUseLongPollInBackgroundChanged: (Boolean) -> Unit,
    onOnlineChanged: (Boolean) -> Unit,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModelImpl>()
) {
    val view = LocalView.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val hapticType = screenState.useHaptics
    if (hapticType != HapticType.None) {
        view.performHapticFeedback(hapticType.getHaptic())
        viewModel.onHapticsUsed()
    }

    SettingsScreenContent(
        onBackClick = onBackClick,
        onSettingsItemClicked = { key ->
            when (key) {
                SettingsKeys.KEY_APPEARANCE_LANGUAGE -> {
                    navigateToLanguagePicker()
                }

                else -> viewModel.onSettingsItemClicked(key)
            }
        },
        onSettingsItemLongClicked = viewModel::onSettingsItemLongClicked,
        onSettingsItemChanged = { key, newValue ->
            when (key) {
                SettingsKeys.KEY_APPEARANCE_MULTILINE -> {
                    val isUsing = newValue as? Boolean ?: false
                    onUseMultilineChanged(isUsing)
                }

                SettingsKeys.KEY_APPEARANCE_DARK_THEME -> {
                    val newMode = newValue as? Int ?: return@SettingsScreenContent
                    AppCompatDelegate.setDefaultNightMode(newMode)

                    val isUsing = isUsingDarkTheme()
                    onUseDarkThemeChanged(isUsing)
                }

                SettingsKeys.KEY_APPEARANCE_AMOLED_THEME -> {
                    val isUsing = newValue as? Boolean ?: false
                    onUseAmoledThemeChanged(isUsing)
                }

                SettingsKeys.KEY_USE_DYNAMIC_COLORS -> {
                    val isUsing = newValue as? Boolean ?: false
                    onUseDynamicColorsChanged(isUsing)
                }

                SettingsKeys.KEY_APPEARANCE_BLUR -> {
                    val isUsing = newValue as? Boolean ?: false
                    onUseBlurChanged(isUsing)
                }

                SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                    val isUsing = newValue as? Boolean ?: false
                    onUseLongPollInBackgroundChanged(isUsing)
                }

                SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS -> {
                    val isUsing = newValue as? Boolean ?: false
                    onOnlineChanged(isUsing)
                }

                else -> viewModel.onSettingsItemChanged(key, newValue)
            }
        },
        screenState = screenState
    )

    HandlePopups(
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

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun SettingsScreenContent(
    onBackClick: () -> Unit,
    onSettingsItemClicked: (key: String) -> Unit,
    onSettingsItemLongClicked: (key: String) -> Unit,
    onSettingsItemChanged: (key: String, newValue: Any?) -> Unit,
    screenState: SettingsScreenState
) {
    val settings: UserSettings = koinInject()

    settings.enableDebugSettings(screenState.showDebugOptions)

    val currentTheme by settings.theme.collectAsStateWithLifecycle()

    val multilineEnabled by settings.multiline.collectAsStateWithLifecycle()

    val settingsList = screenState.settings

    val clickListener = OnSettingsClickListener(onSettingsItemClicked)
    val longClickListener = OnSettingsLongClickListener(onSettingsItemLongClicked)
    val changeListener = OnSettingsChangeListener(onSettingsItemChanged)

    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            val title = @Composable { Text(text = stringResource(id = R.string.title_settings)) }
            val navigationIcon = @Composable {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_arrow_back_24),
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
                        modifier = Modifier.animateItemPlacement()
                    )

                    is SettingsItem.TitleSummary -> TitleSummarySettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        modifier = Modifier.animateItemPlacement()
                    )

                    is SettingsItem.Switch -> SwitchSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener,
                        modifier = Modifier.animateItemPlacement()
                    )

                    is SettingsItem.TextField -> EditTextSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener,
                        modifier = Modifier.animateItemPlacement()
                    )

                    is SettingsItem.ListItem -> ListSettingsItem(
                        item = item,
                        isMultiline = multilineEnabled,
                        onSettingsClickListener = clickListener,
                        onSettingsLongClickListener = longClickListener,
                        onSettingsChangeListener = changeListener,
                        modifier = Modifier.animateItemPlacement()
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
            confirmText = UiText.Resource(R.string.yes),
            confirmAction = positiveClick,
            cancelText = UiText.Resource(R.string.cancel),
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
            text = UiText.Resource(R.string.sign_out_confirm),
            confirmText = positiveText,
            confirmAction = positiveClick,
            cancelText = UiText.Resource(R.string.cancel),
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
            text = UiText.Simple("Long polling in background required notifications permission on Android 13 and up"),
            confirmText = UiText.Simple("Grant"),
            confirmAction = positiveClick,
            cancelText = UiText.Resource(R.string.cancel),
            onDismissAction = dismiss
        )
    }
}
