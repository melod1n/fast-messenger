package dev.meloda.fast.messageshistory.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.meloda.fast.ui.R
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MessageOption(
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int
) : Parcelable {

    data object Retry : MessageOption(
        titleResId = R.string.message_context_action_retry,
        iconResId = R.drawable.ic_restart_alt_round_24
    )

    data object Reply : MessageOption(
        titleResId = R.string.message_context_action_reply,
        iconResId = R.drawable.ic_reply_round_24
    )

    data object ForwardHere : MessageOption(
        titleResId = R.string.message_context_action_forward_here,
        iconResId = R.drawable.ic_reply_all_round_24
    )

    data object Forward : MessageOption(
        titleResId = R.string.message_context_action_forward,
        iconResId = R.drawable.ic_forward_round_24
    )

    data object Pin : MessageOption(
        titleResId = R.string.message_context_action_pin,
        iconResId = R.drawable.ic_keep_round_24
    )

    data object Unpin : MessageOption(
        titleResId = R.string.message_context_action_unpin,
        iconResId = R.drawable.ic_keep_off_round_24
    )

    data object Read : MessageOption(
        titleResId = R.string.message_context_action_read,
        iconResId = R.drawable.ic_done_all_round_24
    )

    data object Copy : MessageOption(
        titleResId = R.string.message_context_action_copy,
        iconResId = R.drawable.ic_content_copy_round_24
    )

    data object MarkAsImportant : MessageOption(
        titleResId = R.string.message_context_action_mark_as_important,
        iconResId = R.drawable.ic_star_fill_round_24
    )

    data object UnmarkAsImportant : MessageOption(
        titleResId = R.string.message_context_action_unmark_as_important,
        iconResId = R.drawable.ic_star_round_24
    )

    data object MarkAsSpam : MessageOption(
        titleResId = R.string.message_context_action_mark_as_spam,
        iconResId = R.drawable.ic_report_round_24
    )

    data object UnmarkAsSpam : MessageOption(
        titleResId = R.string.message_context_action_unmark_as_spam,
        iconResId = R.drawable.ic_report_off_round_24
    )

    data object Edit : MessageOption(
        titleResId = R.string.message_context_action_edit,
        iconResId = R.drawable.ic_edit_round_24
    )

    data object Delete : MessageOption(
        titleResId = R.string.message_context_action_delete,
        iconResId = R.drawable.ic_delete_round_24
    )
}
