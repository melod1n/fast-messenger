package com.meloda.fast.screens.settings

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.common.AppGlobal
import com.microsoft.appcenter.crashes.Crashes

class SettingsPrefsFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener {

    @Suppress("unused")
    companion object {
        const val KeyChangeMultiline = "change_multiline"
        const val ArgEnabled = "enabled"

        const val CategoryAppearance = "appearance"
        const val PrefMultiline = "multiline"

        const val CategoryFeatures = "features"
        const val PrefHideKeyboardOnScroll = "hide_keyboard_on_scroll"
        const val PrefFastText = "fast_text"
        const val PrefFastTextDefaultValue = "¯\\_(ツ)_/¯"

        const val CategoryVisibility = "visibility"
        const val PrefSendOnlineStatus = "send_online_status"

        const val CategoryUpdates = "updates"
        const val PrefCheckUpdates = "check_updates"

        const val CategoryDebug = "debug"
        const val PrefPerformCrash = "perform_crash"
        const val PrefShowDestroyedLongPollAlert = "show_destroyed_long_poll_alert"
        const val PrefShowCrashAlert = "show_crash_alert"

        const val CategoryAppCenter = "msappcenter"
        const val PrefEnableReporter = "msappcenter.enable"
    }

    private val prefs = AppGlobal.preferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        getPreference(PrefMultiline)?.let {
            it.onPreferenceChangeListener = this
        }

        getPreference(PrefCheckUpdates)?.let {
            val version = AppGlobal.versionName.split("_").getOrNull(1)
            val summaryText = getString(R.string.pref_updates_check_update_summary, version)
            it.summary = summaryText

            it.onPreferenceClickListener = this
        }

        getPreference(CategoryDebug)?.let {
            it.isVisible = BuildConfig.DEBUG
        }
        getPreference(PrefPerformCrash)?.let {
            it.isVisible = BuildConfig.DEBUG
            it.onPreferenceClickListener = this
        }

        findPreference<EditTextPreference>(PrefFastText)?.summaryProvider = fastTextSummaryProvider
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return when (preference.key) {
            PrefCheckUpdates -> {
                rootFragment?.openUpdatesScreen()
                true
            }
            PrefPerformCrash -> {
                Crashes.generateTestCrash()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return when (preference.key) {
            PrefMultiline -> {
                val enabled = newValue as Boolean
                setFragmentResult(KeyChangeMultiline, bundleOf(ArgEnabled to enabled))
                true
            }
            else -> false
        }
    }

    private val fastTextSummaryProvider = Preference.SummaryProvider<EditTextPreference> {
        getString(
            R.string.pref_message_fast_text_summary,
            prefs.getString(PrefFastText, PrefFastTextDefaultValue)
        )
    }

    private val rootFragment: SettingsRootFragment? get() = parentFragment as? SettingsRootFragment

    private fun getPreference(key: String) = findPreference<Preference>(key)
}