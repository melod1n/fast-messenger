package ru.melod1n.project.vkm.api.model

import android.graphics.Color
import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKPoll(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    var id = o.optInt("id", -1)
    var ownerId = o.optInt("owner_id", -1)
    var created = o.optInt("created")
    var question: String = o.optString("question")
    var votes = o.optInt("votes")
    var answers = ArrayList<Answer>()
    var isAnonymous = o.optBoolean("anonymous")
    var isMultiple = o.optBoolean("multiple")
    var answerIds = ArrayList<Int>()
    var endDate = o.optInt("end_date")
    var isClosed = o.optBoolean("closed")
    var isBoard = o.optBoolean("is_board")
    var isCanEdit = o.optBoolean("can_edit")
    var isCanVote = false
    var isCanReport = false
    var isCanShare = false
    var authorId = 0
    var background = Color.WHITE

    //TODO: private ArrayList friends

    init {
        o.optJSONArray("answers")?.let {
            val answers = ArrayList<Answer>()
            for (i in 0 until it.length()) {
                answers.add(Answer(it.optJSONObject(i)))
            }
            this.answers = answers
        }

        //setAnswerIds();

        // ...
    }

    class Answer(o: JSONObject) : Serializable {

        var id = o.optInt("id", -1)
        var text: String = o.optString("text")
        var votes = o.optInt("votes")
        var rate = o.optInt("rate")

    }
}