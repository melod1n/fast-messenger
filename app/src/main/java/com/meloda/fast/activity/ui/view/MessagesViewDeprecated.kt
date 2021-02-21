package com.meloda.fast.activity.ui.view

import com.meloda.fast.api.model.VKConversation
import com.meloda.mvp.MvpView

interface MessagesViewDeprecated : MvpView {

    fun showChatPanel()

    fun hideChatPanel()

    fun setWritingAllowed(allowed: Boolean)

    fun setChatInfo(info: String)

    fun openProfile(conversation: VKConversation)

    fun showErrorLoadConversationAlert()

    fun showVoiceRecordingTip()

    fun setMessageText(text: String)

}