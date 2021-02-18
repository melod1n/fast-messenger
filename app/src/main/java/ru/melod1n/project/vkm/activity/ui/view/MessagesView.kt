package ru.melod1n.project.vkm.activity.ui.view

import ru.melod1n.project.vkm.api.model.VKConversation
import ru.melod1n.project.vkm.base.mvp.MvpView

interface MessagesView : MvpView {

    fun showChatPanel()

    fun hideChatPanel()

    fun setWritingAllowed(allowed: Boolean)

    fun setChatInfo(info: String)

    fun openProfile(conversation: VKConversation)

    fun showErrorLoadConversationAlert()

    fun showVoiceRecordingTip()

    fun setMessageText(text: String)

}