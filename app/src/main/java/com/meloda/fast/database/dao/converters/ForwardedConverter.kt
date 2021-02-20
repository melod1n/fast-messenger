package com.meloda.fast.database.dao.converters

import androidx.room.TypeConverter
import com.meloda.fast.api.model.VKMessage
import com.meloda.fast.extensions.ArrayExtensions.isNullOrEmpty
import com.meloda.fast.util.Utils
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