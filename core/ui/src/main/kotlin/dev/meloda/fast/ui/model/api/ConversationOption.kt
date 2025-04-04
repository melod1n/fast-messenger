package dev.meloda.fast.ui.model.api

import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.model.UiText
import dev.meloda.fast.ui.R

sealed class ConversationOption(
    val title: UiText,
    val icon: UiImage
) {

    data object MarkAsRead : ConversationOption(
        title = UiText.Resource(R.string.action_mark_as_read),
        icon = UiImage.Resource(R.drawable.round_done_all_24)
    )

    data object Pin : ConversationOption(
        title = UiText.Resource(R.string.action_pin),
        icon = UiImage.Resource(R.drawable.pin_outline_24)
    )

    data object Unpin : ConversationOption(
        title = UiText.Resource(R.string.action_unpin),
        icon = UiImage.Resource(R.drawable.pin_off_outline_24)
    )

    data object Delete : ConversationOption(
        title = UiText.Resource(R.string.action_delete),
        icon = UiImage.Resource(R.drawable.round_delete_outline_24)
    )

    data object Archive : ConversationOption(
        title = UiText.Resource(R.string.conversation_context_action_archive),
        icon = UiImage.Resource(R.drawable.outline_archive_24)
    )

    data object Unarchive : ConversationOption(
        title = UiText.Resource(R.string.conversation_context_action_unarchive),
        icon = UiImage.Resource(R.drawable.outline_unarchive_24)
    )
}
