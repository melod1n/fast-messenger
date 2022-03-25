package com.meloda.fast.common

import android.os.Bundle
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.screens.conversations.ConversationsFragment
import com.meloda.fast.screens.login.LoginFragment
import com.meloda.fast.screens.main.MainFragment
import com.meloda.fast.screens.messages.MessagesHistoryFragment

@Suppress("FunctionName")
object Screens {
    fun Main() = FragmentScreen { MainFragment() }
    fun Login() = FragmentScreen { LoginFragment() }
    fun Conversations() = FragmentScreen { ConversationsFragment() }
    fun MessagesHistory(bundle: Bundle) =
        FragmentScreen { MessagesHistoryFragment.newInstance(bundle) }
}