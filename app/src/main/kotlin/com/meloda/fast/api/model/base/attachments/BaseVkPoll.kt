package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkPoll(
    val multiple: Boolean,
    val id: Int,
    val votes: Int,
    val anonymous: Boolean,
    val closed: Boolean,
    val end_date: Int,
    val is_board: Boolean,
    val can_vote: Boolean,
    val can_edit: Boolean,
    val can_report: Boolean,
    val can_share: Boolean,
    val created: Int,
    val owner_id: Int,
    val question: String,
    val disable_unvote: Boolean,
    val friends: List<Friend>?,
    val embed_hash: String,
    val answers: List<Answer>,
    val author_id: Int,
    val background: Background?
) : Parcelable {

    @Parcelize
    data class Friend(
        val id: Int
    ) : Parcelable

    @Parcelize
    data class Answer(
        val id: Int,
        val rate: Double,
        val text: String,
        val votes: Int
    ) : Parcelable

    @Parcelize
    data class Background(
        val angle: Int,
        val color: String,
        val id: Int,
        val name: String,
        val type: String,
        val points: List<Point>
    ) : Parcelable {

        @Parcelize
        data class Point(
            val color: String,
            val position: Double
        ) : Parcelable

    }

}
