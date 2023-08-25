package com.meloda.fast.screens.messages

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.screens.messages.presentation.MessagesHistoryFragment

object MessagesHistoryScreens {

    fun messagesHistoryScreen() = FragmentScreen(key = "MessagesHistoryScreen") {
        MessagesHistoryFragment.newInstance()
    }
}
