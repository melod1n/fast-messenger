package com.meloda.fast.screens.messages.screen

import com.github.terrakok.cicerone.Router
import com.meloda.fast.base.screen.AppScreen
import com.meloda.fast.base.screen.createResultFlow
import com.meloda.fast.screens.messages.MessagesHistoryScreens
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.model.MessagesHistoryResult
import kotlin.properties.Delegates

class MessagesHistoryScreen : AppScreen<MessagesHistoryArguments, MessagesHistoryResult> {

    override val resultFlow = createResultFlow()

    override var args: MessagesHistoryArguments by Delegates.notNull()

    override fun show(router: Router, args: MessagesHistoryArguments) {
        this.args = args
        router.navigateTo(MessagesHistoryScreens.messagesHistoryScreen())
    }
}
