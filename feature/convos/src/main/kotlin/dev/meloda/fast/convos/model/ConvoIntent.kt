package dev.meloda.fast.convos.model

import android.os.Bundle
import dev.meloda.fast.ui.model.vk.ConvoOption

sealed class ConvoIntent {
    data class ItemClick(val convoId: Long) : ConvoIntent()
    data class ItemLongClick(val convoId: Long) : ConvoIntent()
    data class OptionItemClick(val option: ConvoOption) : ConvoIntent()
    data object PaginationConditionsMet : ConvoIntent()

    data object Back : ConvoIntent()
    data object Refresh : ConvoIntent()
    data object CreateChatClick : ConvoIntent()
    data object ArchiveClick : ConvoIntent()

    data class SetScrollIndex(val index: Int) : ConvoIntent()
    data class SetScrollOffset(val offset: Int) : ConvoIntent()

    data object ErrorActionButtonClick : ConvoIntent()

    data object ConsumeScrollToTop : ConvoIntent()

    sealed class Dialog : ConvoIntent() {
        data object Dismiss : Dialog()
        data class Confirm(val bundle: Bundle? = null) : Dialog()
        data class Cancel(val bundle: Bundle? = null) : Dialog()
    }
}
