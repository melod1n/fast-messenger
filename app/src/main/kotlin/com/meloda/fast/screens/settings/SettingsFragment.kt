package com.meloda.fast.screens.settings

import android.os.Build
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
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.ext.*
import com.meloda.fast.model.settings.SettingsItem
import com.meloda.fast.screens.main.LongPollState
import com.meloda.fast.screens.main.LongPollUtils
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.screens.settings.items.*
import com.meloda.fast.ui.AppTheme
import com.microsoft.appcenter.crashes.model.TestCrashException
import kotlinx.coroutines.launch

class SettingsFragment : BaseFragment(),
    OnSettingsClickListener,
    OnSettingsLongClickListener,
    OnSettingsChangeListener {

    private var useDynamicColors by mutableStateOf(
        AppGlobal.preferences.getBoolean(
            KEY_USE_DYNAMIC_COLORS,
            DEFAULT_VALUE_USE_DYNAMIC_COLORS
        )
    )
    private var useLargeAppBar by mutableStateOf(
        AppGlobal.preferences.getBoolean(
            "useLargeTopAppBar", false
        )
    )
    private var isMultilineEnabled by mutableStateOf(
        AppGlobal.preferences.getBoolean(
            KEY_APPEARANCE_MULTILINE,
            DEFAULT_VALUE_MULTILINE
        )
    )
    private var settingsList: List<SettingsItem<*>> by mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generateSettings()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as? ComposeView)?.apply {
            setContent {
                SettingsScreen(
                    useDynamicColors = useDynamicColors,
                    useLargeAppBar = useLargeAppBar,
                    isMultiline = isMultilineEnabled,
                    items = settingsList
                )
            }
        }
    }

    private fun generateSettings() {
        val appearanceTitle = SettingsItem.Title.build(
            key = KEY_APPEARANCE,
            title = "Appearance"
        )
        val appearanceMultiline = SettingsItem.Switch.build(
            key = KEY_APPEARANCE_MULTILINE,
            defaultValue = DEFAULT_VALUE_MULTILINE,
            title = "Multiline titles and messages",
            summary = "The title of the dialog and the text of the message can take up two lines"
        )

        val featuresTitle = SettingsItem.Title.build(
            key = "features",
            title = "Features"
        )
        val featuresHideKeyboardOnScroll = SettingsItem.Switch.build(
            key = KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL,
            defaultValue = true,
            title = "Hide keyboard on scroll"
        )
        val featuresFastText = SettingsItem.EditText.build(
            key = KEY_FEATURES_FAST_TEXT,
            title = "Fast text",
            defaultValue = "¯\\_(ツ)_/¯",
        ).apply {
            summaryProvider = SettingsItem.SummaryProvider { settingsItem ->
                getString(
                    R.string.pref_message_fast_text_summary,
                    settingsItem.value.ifEmpty { null }
                )
            }
        }
        val featuresLongPollBackground = SettingsItem.Switch.build(
            key = KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            defaultValue = DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND,
            title = "LongPoll in background",
            summary = "Your messages will be updates even when app is not on the screen"
        )

        val visibilityTitle = SettingsItem.Title.build(
            key = "visibility",
            title = "Visibility"
        )
        val visibilitySendOnlineStatus = SettingsItem.Switch.build(
            key = KEY_VISIBILITY_SEND_ONLINE_STATUS,
            defaultValue = false,
            title = "Send online status",
            summary = "Online status will be sent every five minutes"
        )

        val updatesTitle = SettingsItem.Title.build(
            key = "updates",
            title = "Updates"
        )
        val updatesCheckAtStartup = SettingsItem.Switch.build(
            key = KEY_UPDATES_CHECK_AT_STARTUP,
            title = "Check at startup",
            summary = "Check updates at app startup",
            defaultValue = true
        )
        val updatesCheckUpdates = SettingsItem.TitleSummary.build(
            key = KEY_UPDATES_CHECK_UPDATES,
            title = "Check updates"
        )

        val msAppCenterTitle = SettingsItem.Title.build(
            key = "msappcenter",
            title = "MS AppCenter Crash Reporter"
        )
        val msAppCenterEnable = SettingsItem.Switch.build(
            key = KEY_MS_APPCENTER_ENABLE,
            defaultValue = true,
            title = "Enable Crash Reporter"
        )

        val debugTitle = SettingsItem.Title.build(
            key = "debug",
            title = "Debug"
        )
        val debugPerformCrash = SettingsItem.TitleSummary.build(
            key = KEY_DEBUG_PERFORM_CRASH,
            title = "Perform crash",
            summary = "App will be crashed. Obviously"
        )
        val debugShowDestroyedLongPollAlert = SettingsItem.Switch.build(
            key = KEY_DEBUG_SHOW_DESTROYED_LONG_POLL_ALERT,
            defaultValue = false,
            title = "Show destroyed LP alert"
        )
        val debugShowCrashAlert = SettingsItem.Switch.build(
            key = KEY_DEBUG_SHOW_CRASH_ALERT,
            defaultValue = true,
            title = "Show alert after crash",
            summary = "Shows alert dialog with stacktrace after app crashed\n(it will be not shown if you perform crash manually))"
        )
        val debugUseDynamicColors = SettingsItem.Switch.build(
            key = KEY_USE_DYNAMIC_COLORS,
            title = "[WIP] Use dynamic colors",
            isEnabled = isSdkAtLeast(Build.VERSION_CODES.S),
            summary = "Requires Android 12 or higher;\nUnstable - you may need to manually kill app via it's info screen in order for changes to applied",
            defaultValue = false
        )
        val debugDarkTheme = SettingsItem.ListItem.build(
            key = KEY_APPEARANCE_DARK_THEME,
            title = "[WIP] Dark theme",
            values = listOf(
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            ),
            valueTitles = listOf(
                "Enabled",
                "Disabled",
                "Follow system",
                "Battery saver"
            ),
            defaultValue = AppCompatDelegate.MODE_NIGHT_NO
        )

        val debugUseLargeTopAppBar = SettingsItem.Switch.build(
            key = "useLargeTopAppBar",
            title = "Use LargeTopAppBar",
            defaultValue = false
        )

        val debugHideDebugList = SettingsItem.TitleSummary.build(
            key = KEY_DEBUG_HIDE_DEBUG_LIST,
            title = "Hide debug list"
        )

        val appearanceList: List<SettingsItem<*>> = listOf(
            appearanceTitle,
            appearanceMultiline
        )
        val featuresList = listOf(
            featuresTitle,
            featuresHideKeyboardOnScroll,
            featuresFastText,
            featuresLongPollBackground
        )
        val visibilityList = listOf(
            visibilityTitle,
            visibilitySendOnlineStatus,
        )
        val updatesList = listOf(
            updatesTitle,
            updatesCheckAtStartup,
            updatesCheckUpdates,
        )
        val msAppCenterList = listOf(
            msAppCenterTitle,
            msAppCenterEnable,
        )
        val debugList = mutableListOf<SettingsItem<*>>()
        listOf(
            debugTitle,
            debugPerformCrash,
            debugShowDestroyedLongPollAlert,
            debugShowCrashAlert,
            debugUseDynamicColors,
            debugDarkTheme,
            debugUseLargeTopAppBar,
        ).forEach(debugList::add)

        debugList += debugHideDebugList

        val settingsList = mutableListOf<SettingsItem<*>>()
        listOf(
            appearanceList,
            featuresList,
            visibilityList,
            updatesList,
            msAppCenterList,
            debugList,
        ).forEach(settingsList::addAll)

        if (!AppGlobal.preferences.getBoolean(KEY_SHOW_DEBUG_CATEGORY, false)) {
            settingsList.removeAll(debugList)
        }

        this.settingsList = settingsList
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(
        useDynamicColors: Boolean,
        useLargeAppBar: Boolean,
        isMultiline: Boolean,
        items: List<SettingsItem<*>>
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState()
        )
        val scaffoldModifier = if (useLargeAppBar) {
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            Modifier.fillMaxSize()
        }

        val listenersContext = this

        AppTheme(dynamicColors = useDynamicColors) {
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
                    if (useLargeAppBar) {
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
                        count = items.size,
                        key = { index ->
                            val item = items[index]
                            (item.title ?: item.summary).notNull()
                        }
                    ) { index ->
                        when (val item = items[index]) {
                            is SettingsItem.Title -> TitleSettingsItem(
                                item = item,
                                isMultiline = isMultiline
                            )
                            is SettingsItem.TitleSummary -> TitleSummarySettingsItem(
                                item = item,
                                isMultiline = isMultiline,
                                onSettingsClickListener = listenersContext,
                                onSettingsLongClickListener = listenersContext
                            )
                            is SettingsItem.Switch -> SwitchSettingsItem(
                                item = item,
                                isMultiline = isMultiline,
                                onSettingsClickListener = listenersContext,
                                onSettingsLongClickListener = listenersContext,
                                onSettingsChangeListener = listenersContext
                            )
                            is SettingsItem.EditText -> EditTextSettingsItem(
                                item = item,
                                isMultiline = isMultiline,
                                onSettingsClickListener = listenersContext,
                                onSettingsLongClickListener = listenersContext,
                                onSettingsChangeListener = listenersContext
                            )
                            is SettingsItem.ListItem -> ListSettingsItem(
                                item = item,
                                isMultiline = isMultiline,
                                onSettingsClickListener = listenersContext,
                                onSettingsLongClickListener = listenersContext,
                                onSettingsChangeListener = listenersContext
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onClick(key: String) {
        when (key) {
            KEY_UPDATES_CHECK_UPDATES -> {
                activityRouter?.navigateTo(Screens.Updates())
            }

            KEY_DEBUG_PERFORM_CRASH -> {
                throw TestCrashException()
            }

            KEY_DEBUG_HIDE_DEBUG_LIST -> {
                val showDebugCategory =
                    AppGlobal.preferences.getBoolean(KEY_SHOW_DEBUG_CATEGORY, false)
                if (!showDebugCategory) return

                AppGlobal.preferences.edit {
                    putBoolean(KEY_SHOW_DEBUG_CATEGORY, false)
                }

                generateSettings()
            }
            else -> Unit
        }
    }

    override fun onLongClick(key: String): Boolean {
        return when (key) {
            KEY_UPDATES_CHECK_UPDATES -> {
                val showDebugCategory =
                    AppGlobal.preferences.getBoolean(KEY_SHOW_DEBUG_CATEGORY, false)
                if (showDebugCategory) return false

                AppGlobal.preferences.edit {
                    putBoolean(KEY_SHOW_DEBUG_CATEGORY, true)
                }
                generateSettings()
                true
            }
            else -> false
        }
    }

    override fun onChange(key: String, newValue: Any?) {
        when (key) {
            KEY_APPEARANCE_MULTILINE -> {
                isMultilineEnabled = (newValue as? Boolean).isTrue
            }
            KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                LongPollUtils.requestNotificationsPermission(
                    fragmentActivity = requireActivity(),
                    onStateChangedAction = this::changeLongPollState,
                    fromSettings = true
                )
            }
            KEY_USE_DYNAMIC_COLORS -> {
                useDynamicColors = (newValue as? Boolean).isTrue
            }
            KEY_APPEARANCE_DARK_THEME -> {
                val newMode = newValue as? Int ?: return
                AppCompatDelegate.setDefaultNightMode(newMode)
            }
            "useLargeTopAppBar" -> {
                useLargeAppBar = (newValue as? Boolean).isTrue
            }
            else -> Unit
        }
    }

    private fun changeLongPollState(state: LongPollState) = lifecycleScope.launch {
        MainActivity.longPollState.emit(state)
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()

        const val KEY_APPEARANCE = "appearance"
        const val KEY_APPEARANCE_MULTILINE = "appearance_multiline"
        const val DEFAULT_VALUE_MULTILINE = true

        const val KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL = "features_hide_keyboard_on_scroll"
        const val KEY_FEATURES_FAST_TEXT = "features_fast_text"
        const val DEFAULT_VALUE_FEATURES_FAST_TEXT = "¯\\_(ツ)_/¯"
        const val KEY_FEATURES_LONG_POLL_IN_BACKGROUND = "features_lp_background"
        const val DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND = true

        const val KEY_VISIBILITY_SEND_ONLINE_STATUS = "visibility_send_online_status"

        const val KEY_UPDATES_CHECK_AT_STARTUP = "updates_check_at_startup"
        const val KEY_UPDATES_CHECK_UPDATES = "updates_check_updates"

        const val KEY_MS_APPCENTER_ENABLE = "msappcenter.enable"

        const val KEY_DEBUG_PERFORM_CRASH = "debug_perform_crash"
        const val KEY_USE_DYNAMIC_COLORS = "debug_use_dynamic_colors"
        const val DEFAULT_VALUE_USE_DYNAMIC_COLORS = false
        const val KEY_DEBUG_SHOW_CRASH_ALERT = "debug_show_crash_alert"
        const val KEY_DEBUG_SHOW_DESTROYED_LONG_POLL_ALERT = "debug_show_destroyed_long_poll_alert"
        const val KEY_APPEARANCE_DARK_THEME = "debug_appearance_dark_theme"
        const val DEFAULT_VALUE_APPEARANCE_DARK_THEME = AppCompatDelegate.MODE_NIGHT_NO

        private const val KEY_DEBUG_HIDE_DEBUG_LIST = "debug_hide_debug_list"

        private const val KEY_SHOW_DEBUG_CATEGORY = "show_debug_category"
    }
}
