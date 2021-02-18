package ru.melod1n.project.vkm.fragment.ui.view

import android.os.Bundle
import ru.melod1n.project.vkm.base.mvp.MvpView

interface FriendsView : MvpView {

    fun openChat(extras: Bundle)

}