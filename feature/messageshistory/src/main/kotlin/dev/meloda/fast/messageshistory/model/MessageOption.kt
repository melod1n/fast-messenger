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
        iconResId = R.drawable.round_restart_alt_24
    )

    data object Reply : MessageOption(
        titleResId = R.string.message_context_action_reply,
        iconResId = R.drawable.round_reply_24
    )

    data object ForwardHere : MessageOption(
        titleResId = R.string.message_context_action_forward_here,
        iconResId = R.drawable.round_reply_all_24
    )

    data object Forward : MessageOption(
        titleResId = R.string.message_context_action_forward,
        iconResId = R.drawable.round_forward_24
    )

    data object Pin : MessageOption(
        titleResId = R.string.message_context_action_pin,
        iconResId = R.drawable.pin_outline_24
    )

    data object Unpin : MessageOption(
        titleResId = R.string.message_context_action_unpin,
        iconResId = R.drawable.pin_off_outline_24
    )

    data object Read : MessageOption(
        titleResId = R.string.message_context_action_read,
        iconResId = R.drawable.round_mark_email_read_24
    )

    data object Copy : MessageOption(
        titleResId = R.string.message_context_action_copy,
        iconResId = R.drawable.round_content_copy_24
    )

    data object MarkAsImportant : MessageOption(
        titleResId = R.string.message_context_action_mark_as_important,
        iconResId = R.drawable.round_star_24
    )

    data object UnmarkAsImportant : MessageOption(
        titleResId = R.string.message_context_action_unmark_as_important,
        iconResId = R.drawable.round_star_outline_24
    )

    data object MarkAsSpam : MessageOption(
        titleResId = R.string.message_context_action_mark_as_spam,
        iconResId = R.drawable.round_report_gmailerrorred_24
    )

    data object UnmarkAsSpam : MessageOption(
        titleResId = R.string.message_context_action_unmark_as_spam,
        iconResId = R.drawable.round_report_off_24
    )

    data object Edit : MessageOption(
        titleResId = R.string.message_context_action_edit,
        iconResId = R.drawable.round_create_24
    )

    data object Delete : MessageOption(
        titleResId = R.string.message_context_action_delete,
        iconResId = R.drawable.round_delete_outline_24
    )
}
