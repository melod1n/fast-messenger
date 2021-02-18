package ru.melod1n.project.vkm.database.dao.converters

import androidx.room.TypeConverter
import ru.melod1n.project.vkm.api.model.VKMessage
import ru.melod1n.project.vkm.extensions.ArrayExtensions.isNullOrEmpty
import ru.melod1n.project.vkm.util.Utils
import java.util.*

@Suppress("UNCHECKED_CAST")
class ForwardedConverter {

    @TypeConverter
    fun toForwarded(data: ByteArray?): ArrayList<VKMessage> {
        return if (data.isNullOrEmpty()) arrayListOf() else {
            val deserializedData = Utils.deserialize(data)
            if (deserializedData == null) arrayListOf() else deserializedData as ArrayList<VKMessage>
        }
    }

    @TypeConverter
    fun fromForwarded(forwarded: List<VKMessage>): ByteArray {
        return Utils.serialize(forwarded) ?: return byteArrayOf()
    }

}