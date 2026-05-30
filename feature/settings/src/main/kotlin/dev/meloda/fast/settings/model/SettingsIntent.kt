package dev.meloda.fast.settings.model

import android.os.Bundle

sealed class SettingsIntent {
    data object BackClick : SettingsIntent()

    data class ItemClick(val key: String) : SettingsIntent()
    data class ItemLongClick(val key: String) : SettingsIntent()
    data class ItemValueChanged(val key: String, val newValue: Any?) : SettingsIntent()

    data object ConsumePerformHaptic : SettingsIntent()

    sealed class Dialog : SettingsIntent() {
        data object Dismiss : Dialog()
        data class ConfirmClick(val bundle: Bundle? = null) : Dialog()
        data object CancelClick : Dialog()
        data class ItemPick(val bundle: Bundle? = null) : Dialog()
    }
}
