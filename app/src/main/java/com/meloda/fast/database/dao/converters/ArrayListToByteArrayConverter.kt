package com.meloda.fast.database.dao.converters

import androidx.room.TypeConverter
import com.meloda.fast.api.model.VKModel
import com.meloda.fast.extensions.ArrayExtensions.isNullOrEmpty
import com.meloda.fast.util.Utils

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