package com.meloda.fast.screens.settings.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.*
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.main.MainFragment
import com.meloda.fast.screens.main.activity.LongPollUtils
import com.meloda.fast.screens.main.activity.MainActivity
import com.meloda.fast.screens.settings.HapticType
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
import com.meloda.fast.ui.AppTheme
import kotlinx.coroutines.flow.update
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment() {

    private val viewModel: SettingsViewModel by viewModel<SettingsViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenViewModel()

        (view as? ComposeView)?.setContent {
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()
            val useDynamicColors = screenState.useDynamicColors

            val hapticType = screenState.useHaptics
            if (hapticType != HapticType.None) {
                view.performHapticFeedback(hapticType.getHaptic())
                viewModel.onHapticsUsed()
            }
            AppTheme(useDynamicColors = useDynamicColors) {
                SettingsScreen(
                    onSettingsItemClicked = viewModel::onSettingsItemClicked,
                    onSettingsItemLongClicked = viewModel::onSettingsItemLongClicked,
                    onSettingsItemChanged = viewModel::onSettingsItemChanged,
                    screenState = screenState
                )

                HandleDialogs(screenState = screenState)
            }
        }
    }

    private fun listenViewModel() {
        viewModel.isLongPollBackgroundEnabled.listenValue(::handleLongPollEnabled)
    }

    private fun handleLongPollEnabled(newValue: Boolean?) {
        if (newValue == null) return

        // TODO: 08.04.2023, Danil Nikolaev: rewrite this
        LongPollUtils.requestNotificationsPermission(
            fragmentActivity = requireActivity(),
            onStateChangedAction = { newState -> MainActivity.longPollState.update { newState } },
            fromSettings = true
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(
        onSettingsItemClicked: (key: String) -> Unit,
        onSettingsItemLongClicked: (key: String) -> Unit,
        onSettingsItemChanged: (key: String, newValue: Any?) -> Unit,
        screenState: SettingsScreenState
    ) {
        val useLargeTopAppBar = screenState.useLargeTopAppBar
        val multilineEnabled = screenState.multilineEnabled

        val settings = screenState.settings

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState()
        )
        val scaffoldModifier = if (useLargeTopAppBar) {
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            Modifier.fillMaxSize()
        }

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
            modifier = scaffoldModifier,
            topBar = {
                val title = @Composable { Text(text = "Settings") }
                val navigationIcon = @Composable {
                    IconButton(onClick = { activity?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_arrow_back_24),
                            contentDescription = null
                        )
                    }
                }
                if (useLargeTopAppBar) {
                    LargeTopAppBar(
                        title = title,
                        navigationIcon = navigationIcon,
                        scrollBehavior = scrollBehavior
                    )
                } else {
                    TopAppBar(
                        title = title,
                        navigationIcon = navigationIcon
                    )
                }
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

    // TODO: 25.08.2023, Danil Nikolaev: remove usage of viewModel
    @Composable
    fun HandleDialogs(screenState: SettingsScreenState) {
        val showOptions = screenState.showOptions

        if (showOptions.showPerformCrash) {
            MaterialDialog(
                title = UiText.Simple("Perform Crash"),
                message = UiText.Simple("App will be crashed. Are you sure?"),
                positiveText = UiText.Resource(R.string.yes),
                positiveAction = viewModel::onPerformCrashPositiveButtonClicked,
                negativeText = UiText.Resource(R.string.cancel),
                onDismissAction = viewModel::onPerformCrashAlertDismissed
            )
        }

        if (showOptions.showLogOut) {
            val isEasterEgg = UserConfig.userId == ID_DMITRY

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
                positiveAction = {
                    setFragmentResult(
                        MainFragment.START_SERVICES_KEY,
                        bundleOf(MainFragment.START_SERVICES_ARG_ENABLE to false)
                    )
                    viewModel.onLogOutAlertPositiveClick()
                },
                negativeText = UiText.Resource(R.string.cancel),
                onDismissAction = viewModel::onLogOutAlertDismissed
            )
        }
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()

        const val KEY_ACCOUNT = "account"
        const val KEY_ACCOUNT_LOGOUT = "account_logout"

        const val KEY_APPEARANCE = "appearance"
        const val KEY_APPEARANCE_MULTILINE = "appearance_multiline"
        const val DEFAULT_VALUE_MULTILINE = true
        const val KEY_APPEARANCE_DARK_THEME = "appearance_appearance_dark_theme"
        const val DEFAULT_VALUE_APPEARANCE_DARK_THEME = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        const val KEY_USE_DYNAMIC_COLORS = "appearance_use_dynamic_colors"
        const val DEFAULT_VALUE_USE_DYNAMIC_COLORS = false

        const val KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL = "features_hide_keyboard_on_scroll"
        const val KEY_FEATURES_FAST_TEXT = "features_fast_text"
        const val DEFAULT_VALUE_FEATURES_FAST_TEXT = "¯\\_(ツ)_/¯"
        const val KEY_FEATURES_LONG_POLL_IN_BACKGROUND = "features_lp_background"
        const val DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND = false

        const val KEY_VISIBILITY_SEND_ONLINE_STATUS = "visibility_send_online_status"

        const val KEY_UPDATES_CHECK_AT_STARTUP = "updates_check_at_startup"
        const val KEY_UPDATES_CHECK_UPDATES = "updates_check_updates"

        const val KEY_MS_APPCENTER_ENABLE = "msappcenter.enable"
        const val KEY_MS_APPCENTER_ENABLE_ON_DEBUG = "msappcenter.enable_on_debug"

        const val KEY_DEBUG_PERFORM_CRASH = "debug_perform_crash"

        const val KEY_DEBUG_SHOW_CRASH_ALERT = "debug_show_crash_alert"

        const val KEY_DEBUG_HIDE_DEBUG_LIST = "debug_hide_debug_list"

        const val KEY_USE_LARGE_TOP_APP_BAR = "wip_large_top_app_bar"
        const val DEFAULT_VALUE_USE_LARGE_TOP_APP_BAR = true
        const val KEY_SHOW_EXACT_TIME_ON_TIME_STAMP = "wip_show_exact_time_on_time_stamp"

        const val KEY_SHOW_DEBUG_CATEGORY = "show_debug_category"

        const val ID_DMITRY = 37610580
    }
}

interface AlertDialogListener {
    fun positiveClick()
    fun negativeClick()
    fun neutralClick()
    fun dismiss()
}
