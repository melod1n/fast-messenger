package com.meloda.app.fast.common.model
sealed class LongPollState {
    data object Stopped : LongPollState()

    // TODO: 15/07/2024, Danil Nikolaev: support for android 15
//    data object Terminated : LongPollState()
    data object InApp : LongPollState()
    data object Background : LongPollState()
    data object Exception : LongPollState()


    fun isLaunched(): Boolean = this in listOf(InApp, Background)
}
