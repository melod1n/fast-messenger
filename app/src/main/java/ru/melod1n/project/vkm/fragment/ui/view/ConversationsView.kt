package ru.melod1n.project.vkm.fragment.ui.view

import android.os.Bundle
import ru.melod1n.project.vkm.base.mvp.MvpView

interface ConversationsView : MvpView {

    fun openChat(extras: Bundle)

}