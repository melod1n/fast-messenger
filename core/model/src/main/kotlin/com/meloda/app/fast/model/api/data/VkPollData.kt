package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkPollDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkPollData(
    val multiple: Boolean,
    val id: Int,
    val votes: Int,
    val anonymous: Boolean?,
    val closed: Boolean,
    @Json(name = "end_date") val endDate: Int,
    @Json(name = "is_board") val isBoard: Boolean,
    @Json(name = "can_vote") val canVote: Boolean,
    @Json(name = "can_edit") val canEdit: Boolean,
    @Json(name = "can_report") val canReport: Boolean,
    @Json(name = "can_share") val canShare: Boolean,
    val created: Int,
    @Json(name = "owner_id") val ownerId: Int,
    val question: String,
    @Json(name = "disable_unvote") val disableUnvote: Boolean,
    val friends: List<Friend>?,
    @Json(name = "embed_hash") val embedHash: String,
    val answers: List<Answer>,
    @Json(name = "author_id") val authorId: Int?,
    val background: Background?
) {

    @JsonClass(generateAdapter = true)
    data class Friend(
        val id: Int
    )

    @JsonClass(generateAdapter = true)
    data class Answer(
        val id: Int,
        val rate: Double,
        val text: String,
        val votes: Int
    )

    @JsonClass(generateAdapter = true)
    data class Background(
        val angle: Int,
        val color: String,
        val id: Int,
        val name: String,
        val type: String,
        val points: List<Point>
    ) {

        @JsonClass(generateAdapter = true)
        data class Point(
            val color: String,
            val position: Double
        )
    }

    fun toDomain() = VkPollDomain(id = id)
}
