package dev.meloda.fast.ui.model.vk

import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.model.UiText
import dev.meloda.fast.ui.R

sealed class ConvoOption(
    val title: UiText,
    val icon: UiImage
) {

    data object MarkAsRead : ConvoOption(
        title = UiText.Resource(R.string.action_mark_as_read),
        icon = UiImage.Resource(R.drawable.ic_done_all_round_24)
    )

    data object Pin : ConvoOption(
        title = UiText.Resource(R.string.action_pin),
        icon = UiImage.Resource(R.drawable.ic_keep_round_24)
    )

    data object Unpin : ConvoOption(
        title = UiText.Resource(R.string.action_unpin),
        icon = UiImage.Resource(R.drawable.ic_keep_off_round_24)
    )

    data object Delete : ConvoOption(
        title = UiText.Resource(R.string.action_delete),
        icon = UiImage.Resource(R.drawable.ic_delete_round_24)
    )

    data object Archive : ConvoOption(
        title = UiText.Resource(R.string.convo_context_action_archive),
        icon = UiImage.Resource(R.drawable.ic_archive_round_24)
    )

    data object Unarchive : ConvoOption(
        title = UiText.Resource(R.string.convo_context_action_unarchive),
        icon = UiImage.Resource(R.drawable.ic_unarchive_round_24)
    )
}
