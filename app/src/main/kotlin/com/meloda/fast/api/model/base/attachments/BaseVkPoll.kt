package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkPoll(
    val multiple: Boolean,
    val id: Int,
    val votes: Int,
    val anonymous: Boolean,
    val closed: Boolean,
    @SerializedName("end_date")
    val endDate: Int,
    @SerializedName("is_board")
    val isBoard: Boolean,
    @SerializedName("can_vote")
    val canVote: Boolean,
    @SerializedName("can_edit")
    val canEdit: Boolean,
    @SerializedName("can_report")
    val canReport: Boolean,
    @SerializedName("can_share")
    val canShare: Boolean,
    val created: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    val question: String,
    @SerializedName("disable_unvote")
    val disableUnVote: Boolean,
    val friends: List<Friend>?,
    @SerializedName("embed_hash")
    val embedHash: String,
    val answers: List<Answer>,
    @SerializedName("author_id")
    val authorId: Int,
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
