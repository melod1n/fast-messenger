package com.meloda.fast.screens.settings

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute

class SettingsPrefsFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener {

    @Suppress("unused")
    companion object {
        const val KeyChangeMultiline = "change_multiline"
        const val ArgEnabled = "enabled"

        const val CategoryAppearance = "appearance"
        const val PrefMultiline = "appearance_multiline"

        const val CategoryUpdates = "updates"
        const val PrefCheckUpdates = "updates_check_updates"

        const val CategoryMisc = "misc"
        const val PrefPerformCrash = "misc_perform_crash"

        const val CategoryAcra = "msappcenter"
        const val PrefEnableReporter = "msappcenter.enable"
    }

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

        getPreference(CategoryMisc)?.let {
            it.isVisible = UserConfig.userId == 362877006
        }
        getPreference(PrefPerformCrash)?.let {
            it.isVisible = UserConfig.userId == 362877006
            it.onPreferenceClickListener = this
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return when (preference.key) {
            PrefCheckUpdates -> {
                Distribute.checkForUpdate()
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

    private val rootFragment: SettingsRootFragment? get() = parentFragment as? SettingsRootFragment

    private fun getPreference(key: String) = findPreference<Preference>(key)
}