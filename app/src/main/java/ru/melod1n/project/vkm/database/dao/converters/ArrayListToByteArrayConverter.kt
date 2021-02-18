package ru.melod1n.project.vkm.database.dao.converters

import androidx.room.TypeConverter
import ru.melod1n.project.vkm.api.model.VKModel
import ru.melod1n.project.vkm.extensions.ArrayExtensions.isNullOrEmpty
import ru.melod1n.project.vkm.util.Utils

@Suppress("UNCHECKED_CAST")
class ArrayListToByteArrayConverter {

    @TypeConverter
    fun toForwarded(data: ByteArray?): ArrayList<VKModel> {
        return if (data.isNullOrEmpty()) arrayListOf() else {
            val deserializedData = Utils.deserialize(data)
            if (deserializedData == null) arrayListOf() else deserializedData as ArrayList<VKModel>
        }
    }

    @TypeConverter
    fun fromForwarded(forwarded: List<VKModel>): ByteArray {
        return Utils.serialize(forwarded) ?: return byteArrayOf()
    }

}