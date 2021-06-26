package com.meloda.fast.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKDocument() : VKModel() {

    override val attachmentType = VKAttachments.Type.DOCUMENT

    companion object {
        const val serialVersionUID: Long = 1L
    }

    var id: Int = 0
    var ownerId: Int = 0
    var title: String = ""
    var size: Int = 0
    var ext: String = ""
    var url: String = ""
    var date: Int = 0
    var type: Type = Type.UNKNOWN
    var preview: Preview? = null

    constructor(o: JSONObject) : this() {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        title = o.optString("title")
        size = o.optInt("size")
        ext = o.optString("ext")
        url = o.optString("url")
        date = o.optInt("date")
        type = Type.fromInt(o.optInt("type"))

        o.optJSONObject("preview")?.let {
            preview = Preview(it)
        }
    }

    class Preview(o: JSONObject) : Serializable {
        companion object {
            const val serialVersionUID: Long = 1L
        }

        var photo: Photo? = null
        var graffiti: Graffiti? = null

        inner class Photo(o: JSONObject) : Serializable {

            var sizes: ArrayList<VKPhotoSize>? = null

            init {
                o.optJSONArray("sizes")?.let {
                    val sizes = ArrayList<VKPhotoSize>()
                    for (i in 0 until it.length()) {
                        sizes.add(VKPhotoSize(it.optJSONObject(i)))
                    }
                    this.sizes = sizes
                }
            }
        }

        class Graffiti(o: JSONObject) : Serializable {

            companion object {
                const val serialVersionUID: Long = 1L
            }

            var src: String = o.optString("src")
            var width: Int = o.optInt("width")
            var height: Int = o.optInt("height")
        }

        init {
            o.optJSONObject("photo")?.let {
                photo = Photo(it)
            }

            o.optJSONObject("graffiti")?.let {
                graffiti = Graffiti(it)
            }

        }
    }

    enum class Type(val value: Int) {
        NONE(0),
        TEXT(1),
        ARCHIVE(2),
        GIF(3),
        IMAGE(4),
        AUDIO(5),
        VIDEO(6),
        BOOK(7),
        UNKNOWN(8);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }
}