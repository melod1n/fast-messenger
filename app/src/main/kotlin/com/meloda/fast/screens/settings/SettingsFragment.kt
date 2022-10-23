package com.meloda.fast.screens.settings

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentSettingsBinding
import com.meloda.fast.ext.ifEmpty
import com.meloda.fast.model.settings.SettingsItem
import com.microsoft.appcenter.crashes.Crashes
import dev.chrisbanes.insetter.applyInsetter

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    init {
        useInsets = false
    }

    private val binding by viewBinding(FragmentSettingsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareView()

        val appearanceTitle = SettingsItem.Title(
            title = "Appearance",
            itemKey = "appearance"
        )
        val appearanceMultiline = SettingsItem.Switch(
            itemKey = "multiline",
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
            itemKey = "check_updates",
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
            itemKey = "perform_crash",
            title = "Perform crash",
            summary = "App will be crashed. Obviously"
        )
        val debugShowDestroyedLongPollAlert = SettingsItem.Switch(
            itemKey = "show_destroyed_long_poll_alert",
            defaultValue = false,
            title = "Show destroyed LP alert"
        )
        val debugShowCrashAlert = SettingsItem.Switch(
            itemKey = "show_crash_alert",
            defaultValue = true,
            title = "Show alert after crash",
            summary = "Shows alert dialog with stacktrace after app crashed"
        )

        val appearanceList = listOf(
            appearanceTitle, appearanceMultiline
        )
        val featuresList = listOf(
            featuresTitle, featuresHideKeyboardOnScroll, featuresFastText
        )
        val visibilityList = listOf(
            visibilityTitle, visibilitySendOnlineStatus
        )
        val updatesList = listOf(
            updatesTitle, updatesCheckUpdates
        )
        val msAppCenterList = listOf(
            msAppCenterTitle, msAppCenterEnable
        )
        val debugList = listOf(
            debugTitle, debugPerformCrash, debugShowDestroyedLongPollAlert, debugShowCrashAlert
        )

        val settingsList = mutableListOf<SettingsItem<*>>()
        listOf(
            appearanceList, featuresList, visibilityList,
            updatesList, msAppCenterList, debugList
        ).forEach(settingsList::addAll)

        val adapter = SettingsAdapter(requireContext(), settingsList)
        binding.recyclerView.adapter = adapter

        adapter.onClickAction = { key: String ->
            when (key) {
                updatesCheckUpdates.key -> {
                    requireActivityRouter().navigateTo(Screens.Updates())
                }
                debugPerformCrash.key -> {
                    Crashes.generateTestCrash()
                }
                else -> Unit
            }
        }
        adapter.onChangeAction = { key: String, newValue: Any? ->
            when (key) {
                featuresFastText.key -> {
                    val stringValue = newValue as? String
                    adapter.searchIndex(featuresFastText.key)?.let { index ->
                        val currentItem = adapter[index] as SettingsItem.EditText
                        currentItem.value = stringValue
                        currentItem.updateSummary()

                        adapter.notifyItemChanged(index)
                    }
                }
                else -> Unit
            }
        }
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
}