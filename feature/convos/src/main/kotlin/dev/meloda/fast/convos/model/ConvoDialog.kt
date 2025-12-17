package dev.meloda.fast.convos.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ConvoDialog {
    data class ConvoPin(val convoId: Long) : ConvoDialog()
    data class ConvoUnpin(val convoId: Long) : ConvoDialog()
    data class ConvoDelete(val convoId: Long) : ConvoDialog()
    data class ConvoArchive(val convoId: Long) : ConvoDialog()
    data class ConvoUnarchive(val convoId: Long) : ConvoDialog()
}
