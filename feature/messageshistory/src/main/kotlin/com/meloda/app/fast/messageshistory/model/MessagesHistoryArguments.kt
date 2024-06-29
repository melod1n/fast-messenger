package com.meloda.app.fast.messageshistory.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class MessagesHistoryArguments(val conversationId: Int) : Parcelable
