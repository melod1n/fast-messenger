package com.meloda.fast.screens.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.meloda.fast.R
import com.meloda.fast.common.AppGlobal

class SettingsPrefsFragment : PreferenceFragmentCompat() {

    private companion object {
        private const val CategoryUpdates = "updates"
        private const val PrefCheckUpdates = "updates_check_updates"

        private const val CategoryMisc = "misc"
        private const val PrefPerformCrash = "misc_perform_crash"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val categoryUpdate: Preference? = findPreference(CategoryUpdates)
        val checkUpdates: Preference? = findPreference(PrefCheckUpdates)

        checkUpdates?.run {
            val version = AppGlobal.versionName.split("_").getOrNull(1)
            val summaryText = getString(R.string.pref_updates_check_update_summary, version)
            summary = summaryText

            onPreferenceClickListener = clickListener
        }

        val categoryMisc: Preference? = findPreference(CategoryMisc)
        val performCrash: Preference? = findPreference(PrefPerformCrash)
        performCrash?.run {
            onPreferenceClickListener = clickListener
        }

        val categoryAcra: Preference? = findPreference("acra")
        val enableAcra: Preference? = findPreference("acra.enable")
        enableAcra?.onPreferenceChangeListener = changeListener
    }

    private val clickListener = Preference.OnPreferenceClickListener { preference ->
        return@OnPreferenceClickListener when (preference.key) {
            PrefCheckUpdates -> {
                rootFragment?.openUpdatesScreen()
                true
            }
            PrefPerformCrash -> {
                throw RuntimeException("ur mom gay")
            }
            else -> false
        }
    }

    private val changeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        return@OnPreferenceChangeListener when (preference.key) {
            "acra.enable" -> {
                val enabled = newValue as Boolean

                findPreference<Preference>("acra.alwaysaccept")?.isVisible = enabled
                findPreference<Preference>("acra.deviceid.enable")?.isVisible = enabled
                findPreference<Preference>("acra.syslog.enable")?.isVisible = enabled
                true
            }
            else -> false
        }
    }

    private val rootFragment: SettingsRootFragment? get() = parentFragment as? SettingsRootFragment
}