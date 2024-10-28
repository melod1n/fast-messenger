package dev.meloda.fast.datastore

import dev.meloda.fast.common.model.DarkMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface UserSettings {

    val useContactNames: StateFlow<Boolean>
    val enablePullToRefresh: StateFlow<Boolean>

    val enableMultiline: StateFlow<Boolean>
    val darkMode: StateFlow<DarkMode>
    val enableAmoledDark: StateFlow<Boolean>
    val enableDynamicColors: StateFlow<Boolean>
    val appLanguage: StateFlow<String>

    val fastText: StateFlow<String>

    val sendOnlineStatus: StateFlow<Boolean>

    val showAlertAfterCrash: StateFlow<Boolean>
    val longPollInBackground: StateFlow<Boolean>
    val useBlur: StateFlow<Boolean>
    val showEmojiButton: StateFlow<Boolean>
    val showTimeInActionMessages: StateFlow<Boolean>
    val useSystemFont: StateFlow<Boolean>
    val showDebugCategory: StateFlow<Boolean>

    fun onUseContactNamesChanged(use: Boolean)
    fun onEnablePullToRefreshChanged(enable: Boolean)

    fun onEnableMultilineChanged(enable: Boolean)
    fun onDarkModeChanged(mode: DarkMode)
    fun onEnableAmoledDarkChanged(enable: Boolean)
    fun onEnableDynamicColorsChanged(enable: Boolean)
    fun onAppLanguageChanged(language: String)

    fun onFastTextChanged(text: String)

    fun onSendOnlineStatusChanged(send: Boolean)

    fun onShowAlertAfterCrashChanged(show: Boolean)
    fun onLongPollInBackgroundChanged(inBackground: Boolean)
    fun onUseBlurChanged(use: Boolean)
    fun onShowEmojiButtonChanged(show: Boolean)
    fun onShowTimeInActionMessagesChanged(show: Boolean)
    fun onUseSystemFontChanged(use: Boolean)
    fun onShowDebugCategoryChanged(show: Boolean)
}

class UserSettingsImpl : UserSettings {

    override val useContactNames = MutableStateFlow(AppSettings.General.useContactNames)
    override val enablePullToRefresh = MutableStateFlow(AppSettings.General.enablePullToRefresh)

    override val enableMultiline = MutableStateFlow(AppSettings.Appearance.enableMultiline)
    override val darkMode = MutableStateFlow(AppSettings.Appearance.darkMode)
    override val enableAmoledDark = MutableStateFlow(AppSettings.Appearance.enableAmoledDark)
    override val enableDynamicColors = MutableStateFlow(AppSettings.Appearance.enableDynamicColors)
    override val appLanguage = MutableStateFlow(AppSettings.Appearance.appLanguage)

    override val fastText = MutableStateFlow(AppSettings.Features.fastText)

    override val sendOnlineStatus = MutableStateFlow(AppSettings.Activity.sendOnlineStatus)

    override val showAlertAfterCrash = MutableStateFlow(AppSettings.Debug.showAlertAfterCrash)
    override val longPollInBackground = MutableStateFlow(AppSettings.Debug.longPollInBackground)
    override val useBlur = MutableStateFlow(AppSettings.Debug.useBlur)
    override val showEmojiButton = MutableStateFlow(AppSettings.Debug.showEmojiButton)
    override val showTimeInActionMessages =
        MutableStateFlow(AppSettings.Debug.showTimeInActionMessages)
    override val useSystemFont = MutableStateFlow(AppSettings.Debug.useSystemFont)
    override val showDebugCategory = MutableStateFlow(AppSettings.Debug.showDebugCategory)

    override fun onUseContactNamesChanged(use: Boolean) {
        useContactNames.value = use
    }

    override fun onEnablePullToRefreshChanged(enable: Boolean) {
        enablePullToRefresh.value = enable
    }

    override fun onEnableMultilineChanged(enable: Boolean) {
        enableMultiline.value = enable
    }

    override fun onDarkModeChanged(mode: DarkMode) {
        darkMode.value = mode
    }

    override fun onEnableAmoledDarkChanged(enable: Boolean) {
        enableAmoledDark.value = enable
    }

    override fun onEnableDynamicColorsChanged(enable: Boolean) {
        enableDynamicColors.value = enable
    }

    override fun onAppLanguageChanged(language: String) {
        appLanguage.value = language
    }

    override fun onFastTextChanged(text: String) {
        fastText.value = text
    }

    override fun onSendOnlineStatusChanged(send: Boolean) {
        sendOnlineStatus.value = send
    }

    override fun onShowAlertAfterCrashChanged(show: Boolean) {
        showAlertAfterCrash.value = show
    }

    override fun onLongPollInBackgroundChanged(inBackground: Boolean) {
        longPollInBackground.value = inBackground
    }

    override fun onUseBlurChanged(use: Boolean) {
        useBlur.value = use
    }

    override fun onShowEmojiButtonChanged(show: Boolean) {
        showEmojiButton.value = show
    }

    override fun onShowTimeInActionMessagesChanged(show: Boolean) {
        showTimeInActionMessages.value = show
    }

    override fun onUseSystemFontChanged(use: Boolean) {
        useSystemFont.value = use
    }

    override fun onShowDebugCategoryChanged(show: Boolean) {
        showDebugCategory.value = show
    }
}
