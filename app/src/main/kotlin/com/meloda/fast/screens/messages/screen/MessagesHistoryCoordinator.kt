package com.meloda.fast.screens.messages.screen

import com.github.terrakok.cicerone.Router
import com.meloda.fast.screens.messages.model.MessagesHistoryResult
import kotlinx.coroutines.flow.MutableSharedFlow

interface MessagesHistoryCoordinator {

    fun finishWithResult(result: MessagesHistoryResult)
}

class MessagesHistoryCoordinatorImpl(
    private val resultFlow: MutableSharedFlow<MessagesHistoryResult>,
    private val router: Router
) : MessagesHistoryCoordinator {

    override fun finishWithResult(result: MessagesHistoryResult) {
        resultFlow.tryEmit(result)
        router.exit()
    }
}
