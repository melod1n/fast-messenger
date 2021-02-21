package com.meloda.fast.fragment.ui.view

import android.os.Bundle
import com.meloda.mvp.MvpView

interface ConversationsViewDeprecated : MvpView {

    fun openChat(extras: Bundle)

}