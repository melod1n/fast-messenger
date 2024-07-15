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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.datastore.isUsingDarkMode
import com.meloda.app.fast.settings.SettingsViewModel
import com.meloda.app.fast.settings.SettingsViewModelImpl
import com.meloda.app.fast.settings.model.SettingsScreenState
import com.meloda.app.fast.settings.model.UiItem
import com.meloda.app.fast.settings.presentation.item.ListItem
import com.meloda.app.fast.settings.presentation.item.SwitchItem
import com.meloda.app.fast.settings.presentation.item.TextFieldItem
import com.meloda.app.fast.settings.presentation.item.TitleItem
import com.meloda.app.fast.settings.presentation.item.TitleTextItem
import com.meloda.app.fast.ui.components.ActionInvokeDismiss
import com.meloda.app.fast.ui.components.MaterialDialog
import com.meloda.app.fast.ui.theme.LocalTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.meloda.app.fast.ui.R as UiR

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onLogOutButtonClicked: () -> Unit,
    onLanguageItemClicked: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModelImpl>()
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val userSettings: UserSettings = koinInject()

    LaunchedEffect(true) {
        userSettings.enableDebugSettings(screenState.showDebugOptions)
    }

    SettingsScreen(screenState = screenState,
        onBack = onBack,
        onHapticPerformed = viewModel::onHapticPerformed,
        onSettingsItemClicked = { key ->
            when (key) {
                SettingsKeys.KEY_APPEARANCE_LANGUAGE -> {
                    onLanguageItemClicked()
                }

                else -> viewModel.onSettingsItemClicked(key)
            }
        },
        onSettingsItemLongClicked = viewModel::onSettingsItemLongClicked,
        onSettingsItemValueChanged = { key, newValue ->
            viewModel.onSettingsItemChanged(key, newValue)

            when (key) {
                SettingsKeys.KEY_APPEARANCE_DARK_THEME -> {
                    val newMode = newValue as? Int ?: 0
                    AppCompatDelegate.setDefaultNightMode(newMode)

                    val isUsing = context.getSystemService<PowerManager>()?.let { manager ->
                        isUsingDarkMode(
                            context.resources,
                            manager
                        )
                    } ?: false

                    userSettings.useDarkThemeChanged(isUsing)
                }
            }
        }
    )

    HandlePopups(
        performCrashPositiveClick = viewModel::onPerformCrashPositiveButtonClicked,
        performCrashDismissed = viewModel::onPerformCrashAlertDismissed,
        logoutPositiveClick = {
            viewModel.onLogOutAlertPositiveClick()
            onLogOutButtonClicked()
        },
        logoutDismissed = viewModel::onLogOutAlertDismissed,
        screenState = screenState
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun SettingsScreen(
    screenState: SettingsScreenState = SettingsScreenState.EMPTY,
    onBack: () -> Unit = {},
    onHapticPerformed: () -> Unit = {},
    onSettingsItemClicked: (key: String) -> Unit = {},
    onSettingsItemLongClicked: (key: String) -> Unit = {},
    onSettingsItemValueChanged: (key: String, newValue: Any?) -> Unit = { _, _ -> }
) {
    val view = LocalView.current
    val hapticType = screenState.useHaptics

    LaunchedEffect(hapticType) {
        if (hapticType != null) {
            view.performHapticFeedback(hapticType.getHaptic())
            onHapticPerformed()
        }
    }

    val currentTheme = LocalTheme.current

    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = UiR.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = UiR.drawable.ic_round_arrow_back_24),
                            contentDescription = "Back button"
                        )
                    }
                },
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
            item {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            }

            items(
                items = screenState.settings,
                key = UiItem::key,
                contentType = { item ->
                    when (item) {
                        is UiItem.Title -> "title"
                        is UiItem.TitleText -> "title_text"
                        is UiItem.Switch -> "switch"
                        is UiItem.TextField -> "text_field"
                        is UiItem.List<*> -> "list"
                    }
                }
            ) { item ->
                when (item) {
                    is UiItem.Title -> {
                        TitleItem(item = item)
                    }

                    is UiItem.TitleText -> {
                        TitleTextItem(
                            item = item,
                            onClick = onSettingsItemClicked,
                            onLongClick = onSettingsItemLongClicked
                        )
                    }

                    is UiItem.Switch -> {
                        SwitchItem(
                            item = item,
                            onClick = { onSettingsItemClicked(item.key) },
                            onLongClick = { onSettingsItemLongClicked(item.key) },
                            onChanged = { onSettingsItemValueChanged(item.key, it) }
                        )
                    }

                    is UiItem.TextField -> {
                        TextFieldItem(
                            item = item,
                            onClick = { onSettingsItemClicked(item.key) },
                            onLongClick = { onSettingsItemLongClicked(item.key) },
                            onChanged = { onSettingsItemValueChanged(item.key, it) }
                        )
                    }

                    is UiItem.List<*> -> {
                        ListItem(
                            item = item,
                            onClick = { onSettingsItemClicked(item.key) },
                            onLongClick = { onSettingsItemLongClicked(item.key) },
                            onChanged = { onSettingsItemValueChanged(item.key, it) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
fun HandlePopups(
    performCrashPositiveClick: () -> Unit,
    performCrashDismissed: () -> Unit,
    logoutPositiveClick: () -> Unit,
    logoutDismissed: () -> Unit,
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
}

@Composable
fun PerformCrashDialog(
    positiveClick: () -> Unit,
    dismiss: () -> Unit,
    show: Boolean,
) {
    if (show) {
        MaterialDialog(
            onDismissRequest = dismiss,
            title = "Perform crash",
            text = "App will be crashed. Are you sure?",
            confirmAction = positiveClick,
            confirmText = stringResource(id = UiR.string.yes),
            cancelText = stringResource(id = UiR.string.cancel),
            actionInvokeDismiss = ActionInvokeDismiss.Always
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

        MaterialDialog(
            onDismissRequest = dismiss,
            title = stringResource(
                id = if (isEasterEgg) UiR.string.easter_egg_log_out_dmitry
                else UiR.string.sign_out_confirm_title
            ),
            text = stringResource(id = UiR.string.sign_out_confirm),
            confirmAction = positiveClick,
            confirmText = stringResource(
                id = if (isEasterEgg) UiR.string.easter_egg_log_out_dmitry
                else UiR.string.action_sign_out
            ),
            cancelText = stringResource(id = UiR.string.no),
            actionInvokeDismiss = ActionInvokeDismiss.Always
        )
    }
}
