package com.meloda.fast.screens.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import by.kirich1409.viewbindingdelegate.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.adapter.AsyncDiffItemAdapter
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentSettingsBinding
import com.meloda.fast.ext.ifEmpty
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.settings.SettingsItem
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
import kotlin.properties.Delegates

class SettingsFragment : BaseFragment(R.layout.fragment_settings),
    OnSettingsClickListener,
    OnSettingsLongClickListener,
    OnSettingsChangeListener {

    init {
        useInsets = false
    }

    private val binding by viewBinding(FragmentSettingsBinding::bind)

    private var testAppearanceList = mutableListOf<SettingsItem<*>>()

    private val debugList = mutableListOf<SettingsItem<*>>()

    private var adapter by Delegates.notNull<AsyncDiffItemAdapter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appearanceTitle = SettingsItem.Title(
            title = "Appearance",
            itemKey = "appearance"
        )
        val appearanceMultiline = SettingsItem.Switch(
            itemKey = "appearance_multiline",
            defaultValue = true,
            title = "Multiline titles and messages",
            summary = "The title of the dialog and the text of the message can take up two lines"
        )

        val featuresTitle = SettingsItem.Title(
            title = "Features",
            itemKey = "features"
        )
        val featuresHideKeyboardOnScroll = SettingsItem.Switch(
            itemKey = "hide_keyboard_on_scroll",
            defaultValue = true,
            title = "Hide keyboard on scroll"
        )

        val featuresFastText = SettingsItem.EditText(
            itemKey = "fast_text",
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

        val visibilityTitle = SettingsItem.Title(
            itemKey = "visibility",
            title = "Visibility"
        )
        val visibilitySendOnlineStatus = SettingsItem.Switch(
            itemKey = "send_online_status",
            defaultValue = true,
            title = "Send online status",
            summary = "Online status will be sent every five minutes"
        )

        val updatesTitle = SettingsItem.Title(
            itemKey = "updates",
            title = "Updates"
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
            itemKey = "msappcenter.enable",
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
            itemKey = "debug_show_destroyed_long_poll_alert",
            defaultValue = false,
            title = "Show destroyed LP alert"
        )
        val debugShowCrashAlert = SettingsItem.Switch(
            itemKey = "debug_show_crash_alert",
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
            appearanceMultiline,
        )
        val featuresList = listOf(
            featuresTitle,
            featuresHideKeyboardOnScroll,
            featuresFastText,
        )
        val visibilityList = listOf(
            visibilityTitle,
            visibilitySendOnlineStatus,
        )
        val updatesList = listOf(
            updatesTitle,
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
            appearanceList, featuresList, visibilityList,
            updatesList, msAppCenterList, debugList
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
        prepareInsets()
        prepareToolbar()
    }

    private fun prepareInsets() {
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

    @Suppress("UNCHECKED_CAST")
    override fun onChange(key: String, newValue: Any?) {
        when (key) {
            KEY_DEBUG_TEST_THEME -> {
                requireActivity().recreate()
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

    @Suppress("UNCHECKED_CAST")
    private fun List<AdapterDiffItem>.castAsSettings(): List<SettingsItem<*>> {
        return (this as List<SettingsItem<*>>).toList()
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()

        const val KEY_UPDATES_CHECK_UPDATES = "updates_check_updates"

        const val KEY_DEBUG_PERFORM_CRASH = "debug_perform_crash"
        const val KEY_DEBUG_TEST_THEME = "debug_test_theme"
        const val KEY_DEBUG_LIST_UPDATE = "debug_list_update"
        private const val KEY_DEBUG_HIDE_DEBUG_LIST = "debug_hide_debug_list"

        private const val KEY_SHOW_DEBUG_CATEGORY = "show_debug_category"
    }
}
