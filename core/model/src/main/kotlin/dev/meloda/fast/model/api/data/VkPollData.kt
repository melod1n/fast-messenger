package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkPollDomain

@JsonClass(generateAdapter = true)
data class VkPollData(
    val multiple: Boolean,
    val id: Long,
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
    @Json(name = "owner_id") val ownerId: Long,
    val question: String,
    @Json(name = "disable_unvote") val disableUnvote: Boolean,
    val friends: List<Friend>?,
    @Json(name = "embed_hash") val embedHash: String,
    val answers: List<Answer>,
    @Json(name = "author_id") val authorId: Long?,
    val background: Background?
) {

    @JsonClass(generateAdapter = true)
    data class Friend(
        val id: Long
    )

    @JsonClass(generateAdapter = true)
    data class Answer(
        val id: Long,
        val rate: Double,
        val text: String,
        val votes: Int
    )

    @JsonClass(generateAdapter = true)
    data class Background(
        val angle: Int,
        val color: String,
        val id: Long,
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
