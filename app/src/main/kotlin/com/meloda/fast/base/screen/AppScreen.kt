package com.meloda.fast.base.screen

import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

interface AppScreen<ArgType, ResultType> {
    val resultFlow: MutableSharedFlow<ResultType>

    fun show(router: Router, args: ArgType)
}

@Suppress("unused")
fun <ArgType, ResultType> AppScreen<ArgType, ResultType>.createResultFlow(): MutableSharedFlow<ResultType> {
    return MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
}
