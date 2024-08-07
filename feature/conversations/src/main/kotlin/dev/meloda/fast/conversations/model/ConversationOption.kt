package dev.meloda.fast.conversations.model

import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.model.UiText
import dev.meloda.fast.ui.R as UiR

sealed class ConversationOption(
    val title: UiText,
    val icon: UiImage
) {

    data object MarkAsRead : ConversationOption(
        title = UiText.Resource(UiR.string.action_mark_as_read),
        icon = UiImage.Resource(UiR.drawable.round_done_all_24)
    )

    data object Pin : ConversationOption(
        title = UiText.Resource(UiR.string.action_pin),
        icon = UiImage.Resource(UiR.drawable.pin_outline_24)
    )

    data object Unpin : ConversationOption(
        title = UiText.Resource(UiR.string.action_unpin),
        icon = UiImage.Resource(UiR.drawable.pin_off_outline_24)
    )

    data object Delete : ConversationOption(
        title = UiText.Resource(UiR.string.action_delete),
        icon = UiImage.Resource(UiR.drawable.round_delete_outline_24)
    )
}
