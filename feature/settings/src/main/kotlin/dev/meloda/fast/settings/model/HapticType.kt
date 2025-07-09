package dev.meloda.fast.settings.model;

import androidx.core.view.HapticFeedbackConstantsCompat

enum class HapticType {
    LONG_PRESS, REJECT;

    fun getHaptic(): Int = when (this) {
        LONG_PRESS -> HapticFeedbackConstantsCompat.LONG_PRESS
        REJECT -> HapticFeedbackConstantsCompat.REJECT
    }
}
