package ru.melod1n.project.vkm.api.model

import org.json.JSONObject
import java.util.*

class VKDoc(o: JSONObject) : VKModel() {

    constructor() : this(JSONObject())

    companion object {
        const val TYPE_NONE = 0
        const val TYPE_TEXT = 1
        const val TYPE_ARCHIVE = 2
        const val TYPE_GIF = 3
        const val TYPE_IMAGE = 4
        const val TYPE_AUDIO = 5
        const val TYPE_VIDEO = 6
        const val TYPE_BOOK = 7
        const val TYPE_UNKNOWN = 8
    }

    var id = o.optInt("id", -1)
    var ownerId = o.optInt("owner_id", -1)
    var title: String = o.optString("title")
    var size = o.optInt("size")
    var ext: String = o.optString("ext")
    var url: String = o.optString("url")
    var date = o.optInt("date")
    var type = o.optInt("type")
    var preview: Preview? = null

    init {
        o.optJSONObject("preview")?.let {
            preview = Preview(it)
        }
    }

    class Preview(o: JSONObject) {
        var photo: Photo? = null
        var graffiti: Graffiti? = null

        inner class Photo(o: JSONObject) {
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

        class Graffiti(o: JSONObject) {
            var src: String = o.optString("src")
            var width = o.optInt("width")
            var height = o.optInt("height")
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
}