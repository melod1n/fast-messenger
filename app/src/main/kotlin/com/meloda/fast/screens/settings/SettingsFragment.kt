package com.meloda.fast.screens.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.adapter.AsyncDiffItemAdapter
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentSettingsBinding
import com.meloda.fast.ext.ifEmpty
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.settings.SettingsItem
import com.meloda.fast.screens.main.LongPollState
import com.meloda.fast.screens.main.LongPollUtils
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.screens.settings.adapter.OnSettingsChangeListener
import com.meloda.fast.screens.settings.adapter.OnSettingsClickListener
import com.meloda.fast.screens.settings.adapter.OnSettingsLongClickListener
import com.meloda.fast.screens.settings.adapter.settingsCheckboxItemDelegate
import com.meloda.fast.screens.settings.adapter.settingsEditTextItemDelegate
import com.meloda.fast.screens.settings.adapter.settingsSwitchItemDelegate
import com.meloda.fast.screens.settings.adapter.settingsTitleItemDelegate
import com.meloda.fast.screens.settings.adapter.settingsTitleSummaryItemDelegate
import com.microsoft.appcenter.crashes.model.TestCrashException
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SettingsFragment : BaseFragment(R.layout.fragment_settings),
    OnSettingsClickListener,
    OnSettingsLongClickListener,
    OnSettingsChangeListener {

    private val binding by viewBinding(FragmentSettingsBinding::bind)

    private var testAppearanceList = mutableListOf<SettingsItem<*>>()

    private val debugList = mutableListOf<SettingsItem<*>>()

    private var adapter by Delegates.notNull<AsyncDiffItemAdapter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appearanceTitle = SettingsItem.Title(
            title = "Appearance",
            itemKey = KEY_APPEARANCE
        )
        val appearanceDarkTheme = SettingsItem.TitleSummary(
            itemKey = KEY_APPEARANCE_DARK_THEME,
            title = "Dark theme"
        )
        val appearanceMultiline = SettingsItem.Switch(
            itemKey = KEY_APPEARANCE_MULTILINE,
            defaultValue = true,
            title = "Multiline titles and messages",
            summary = "The title of the dialog and the text of the message can take up two lines"
        )
        val appearanceLanguage = SettingsItem.ListItem(
            itemKey = "languages",
            values = listOf("en", "ru"),
            valueTitles = listOf("English", "Russian"),
            title = "Languages",
            summary = "",
            defaultValue = null,
            selectedInt = null
        )

        val featuresTitle = SettingsItem.Title(
            title = "Features",
            itemKey = "features"
        )
        val featuresHideKeyboardOnScroll = SettingsItem.Switch(
            itemKey = KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL,
            defaultValue = true,
            title = "Hide keyboard on scroll"
        )
        val featuresFastText = SettingsItem.EditText(
            itemKey = KEY_FEATURES_FAST_TEXT,
            title = "Fast text",
            defaultValue = "¯\\_(ツ)_/¯",
        ).apply {
            summaryProvider = object : SettingsItem.SummaryProvider<SettingsItem.EditText> {
                override fun provideSummary(settingsItem: SettingsItem.EditText): String {
                    return getString(
                        R.string.pref_message_fast_text_summary,
                        settingsItem.value.ifEmpty { null }
                    )
                }
            }
        }
        val featuresLongPollBackground = SettingsItem.Switch(
            itemKey = KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            defaultValue = DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND,
            title = "LongPoll in background",
            summary = "Your messages will be updates even when app is not on the screen"
        )

        val visibilityTitle = SettingsItem.Title(
            itemKey = "visibility",
            title = "Visibility"
        )
        val visibilitySendOnlineStatus = SettingsItem.Switch(
            itemKey = KEY_VISIBILITY_SEND_ONLINE_STATUS,
            defaultValue = false,
            title = "Send online status",
            summary = "Online status will be sent every five minutes"
        )

        val updatesTitle = SettingsItem.Title(
            itemKey = "updates",
            title = "Updates"
        )
        val updatesCheckAtStartup = SettingsItem.Switch(
            itemKey = KEY_UPDATES_CHECK_AT_STARTUP,
            title = "Check at startup",
            summary = "Check updates at app startup",
            defaultValue = true
        )
        val updatesCheckUpdates = SettingsItem.TitleSummary(
            itemKey = KEY_UPDATES_CHECK_UPDATES,
            title = "Check updates"
        )

        val msAppCenterTitle = SettingsItem.Title(
            itemKey = "msappcenter",
            title = "MS AppCenter Crash Reporter"
        )
        val msAppCenterEnable = SettingsItem.Switch(
            itemKey = KEY_MS_APPCENTER_ENABLE,
            defaultValue = true,
            title = "Enable Crash Reporter"
        )

        val debugTitle = SettingsItem.Title(
            itemKey = "debug",
            title = "Debug"
        )
        val debugPerformCrash = SettingsItem.TitleSummary(
            itemKey = KEY_DEBUG_PERFORM_CRASH,
            title = "Perform crash",
            summary = "App will be crashed. Obviously"
        )
        val debugShowDestroyedLongPollAlert = SettingsItem.Switch(
            itemKey = KEY_DEBUG_SHOW_DESTROYED_LONG_POLL_ALERT,
            defaultValue = false,
            title = "Show destroyed LP alert"
        )
        val debugShowCrashAlert = SettingsItem.Switch(
            itemKey = KEY_DEBUG_SHOW_CRASH_ALERT,
            defaultValue = true,
            title = "Show alert after crash",
            summary = "Shows alert dialog with stacktrace after app crashed\n(it will be not shown if you perform crash manually))"
        )
        val debugTestThemeSwitch = SettingsItem.Switch(
            itemKey = KEY_DEBUG_TEST_THEME,
            title = "Test theme switch",
            defaultValue = false
        )
        val debugListUpdateSwitch = SettingsItem.Switch(
            itemKey = KEY_DEBUG_LIST_UPDATE,
            title = "Show Appearance Category",
            defaultValue = true
        )
        val debugHideDebugList = SettingsItem.TitleSummary(
            itemKey = KEY_DEBUG_HIDE_DEBUG_LIST,
            title = "Hide debug list"
        )

        val appearanceList: List<SettingsItem<*>> = listOf(
            appearanceTitle,
            appearanceDarkTheme,
            appearanceMultiline,
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
        listOf(
            debugTitle,
            debugPerformCrash,
            debugShowDestroyedLongPollAlert,
            debugShowCrashAlert,
            debugTestThemeSwitch,
            debugListUpdateSwitch,
            debugHideDebugList,
        ).forEach(debugList::add)

        val settingsList = mutableListOf<SettingsItem<*>>()
        listOf(
            appearanceList,
            featuresList,
            visibilityList,
            updatesList,
            msAppCenterList,
            debugList,
        ).forEach(settingsList::addAll)

        testAppearanceList = appearanceList.toMutableList()

        val titleDelegate = settingsTitleItemDelegate()
        val titleSummaryDelegate = settingsTitleSummaryItemDelegate(
            onClickListener = this, onLongClickListener = this
        )
        val editTextDelegate = settingsEditTextItemDelegate(
            onClickListener = this, onLongClickListener = this, onChangeListener = this
        )
        val checkboxDelegate = settingsCheckboxItemDelegate(
            onClickListener = this, onLongClickListener = this, onChangeListener = this
        )
        val switchDelegate = settingsSwitchItemDelegate(
            onClickListener = this, onLongClickListener = this, onChangeListener = this
        )

        val adapter = AsyncDiffItemAdapter(
            titleDelegate,
            titleSummaryDelegate,
            editTextDelegate,
            checkboxDelegate,
            switchDelegate
        )
        this.adapter = adapter
        binding.recyclerView.adapter = adapter

        if (!AppGlobal.preferences.getBoolean(KEY_SHOW_DEBUG_CATEGORY, false)) {
            settingsList.removeAll(debugList)
        }

        adapter.items = settingsList.toList()

        prepareView()
    }

    private fun prepareView() {
        applyInsets()
        prepareToolbar()
    }

    private fun applyInsets() {
        binding.appBar.applyInsetter {
            type(statusBars = true) { padding() }
        }
        binding.recyclerView.applyInsetter {
            type(navigationBars = true) { padding() }
        }
    }

    private fun prepareToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    override fun onClick(key: String) {
        when (key) {
            KEY_APPEARANCE_DARK_THEME -> {
                val keys = arrayOf(
                    AppCompatDelegate.MODE_NIGHT_YES,
                    AppCompatDelegate.MODE_NIGHT_NO,
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                )
                val titles = arrayOf(
                    "Enabled", "Disabled", "Follow system", "Battery saver"
                )

                val currentDarkThemeValue =
                    AppGlobal.preferences.getInt(
                        KEY_APPEARANCE_DARK_THEME,
                        DEFAULT_VALUE_APPEARANCE_DARK_THEME
                    )

                val selectedItemIndex = keys.indexOf(currentDarkThemeValue)

                MaterialAlertDialogBuilder(requireContext())
                    .setSingleChoiceItems(titles, selectedItemIndex) { dialog, which ->
                        val newMode = keys[which]
                        AppGlobal.preferences.edit { putInt(KEY_APPEARANCE_DARK_THEME, newMode) }

                        AppCompatDelegate.setDefaultNightMode(newMode)

                        if (newMode != currentDarkThemeValue) {
                            dialog.dismiss()
                        }
                    }
                    .show()

            }

            KEY_UPDATES_CHECK_UPDATES -> {
                requireActivityRouter().navigateTo(Screens.Updates())
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

                adapter.items =
                    adapter.items.castAsSettings().filter { !it.key.startsWith("debug") }
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

                adapter.items = adapter.items.castAsSettings() + debugList
                true
            }

            else -> false
        }
    }

    override fun onChange(key: String, newValue: Any?) {
        when (key) {
            KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                LongPollUtils.requestNotificationsPermission(
                    fragmentActivity = requireActivity(),
                    onStateChangedAction = this::changeLongPollState,
                    fromSettings = true
                )
            }

            KEY_DEBUG_TEST_THEME -> {
//                requireActivity().recreate()
            }

            KEY_DEBUG_LIST_UPDATE -> {
                val showAppearanceCategory = newValue as Boolean

                val currentItems = adapter.items.castAsSettings()

                if (showAppearanceCategory) {
                    adapter.items = testAppearanceList + currentItems
                } else {
                    adapter.items = currentItems.filter { !it.key.startsWith("appearance") }
                }
            }

            else -> Unit
        }
    }

    private fun changeLongPollState(state: LongPollState) = lifecycleScope.launch {
        MainActivity.longPollState.emit(state)
    }

    @Suppress("UNCHECKED_CAST")
    private fun List<AdapterDiffItem>.castAsSettings(): List<SettingsItem<*>> {
        return (this as List<SettingsItem<*>>).toList()
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()

        const val KEY_APPEARANCE = "appearance"
        const val KEY_APPEARANCE_DARK_THEME = "appearance_dark_theme"
        const val DEFAULT_VALUE_APPEARANCE_DARK_THEME = -1
        const val KEY_APPEARANCE_MULTILINE = "appearance_multiline"

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
        const val KEY_DEBUG_TEST_THEME = "debug_test_theme"
        const val KEY_DEBUG_LIST_UPDATE = "debug_list_update"
        const val KEY_DEBUG_SHOW_CRASH_ALERT = "debug_show_crash_alert"
        const val KEY_DEBUG_SHOW_DESTROYED_LONG_POLL_ALERT = "debug_show_destroyed_long_poll_alert"
        private const val KEY_DEBUG_HIDE_DEBUG_LIST = "debug_hide_debug_list"

        private const val KEY_SHOW_DEBUG_CATEGORY = "show_debug_category"
    }
}
