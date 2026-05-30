package dev.meloda.fast.convos.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ConvoDialog {
    data object Pin : ConvoDialog()
    data object Unpin : ConvoDialog()
    data object Delete : ConvoDialog()
    data object Archive : ConvoDialog()
    data object Unarchive : ConvoDialog()
}
