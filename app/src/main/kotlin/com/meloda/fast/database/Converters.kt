package com.meloda.fast.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.meloda.fast.api.base.AttachmentClassNameIsEmptyException
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkAttachment
import com.meloda.fast.api.model.data.VkMessageData
import org.json.JSONObject

@Suppress("UnnecessaryVariable")
class Converters {

    private companion object {
        private const val CACHE_SEPARATOR = "fastkruta228355"
    }

    @TypeConverter
    fun fromGeoToString(geo: VkMessageData.Geo?): String? {
        if (geo == null) return null

        return try {
            val string = Gson().toJson(geo)

            return string
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @TypeConverter
    fun fromStringToGeo(string: String?): VkMessageData.Geo? {
        if (string == null) return null

        return try {
            val geo = Gson().fromJson(string, VkMessageData.Geo::class.java)

            return geo
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @TypeConverter
    fun fromListVkMessageToString(messages: List<VkMessageDomain>?): String? {
        if (messages == null) return null

        val string = messages
            .mapNotNull(::fromVkMessageToString)
            .joinToString(separator = CACHE_SEPARATOR)

        return string
    }

    @TypeConverter
    fun fromStringToListVkMessage(string: String?): List<VkMessageDomain>? {
        if (string == null) return null

        if (string.contains(CACHE_SEPARATOR)) {
            val messages = string
                .split(CACHE_SEPARATOR)
                .mapNotNull(::fromStringToVkMessage)
            return messages
        }


        val message = fromStringToVkMessage(string)
        return message?.let { listOf(it) }
    }

    @TypeConverter
    fun fromVkMessageToString(message: VkMessageDomain?): String? {
        if (message == null) return null

        return try {
            val string = Gson().toJson(message)

            return string
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @TypeConverter
    fun fromStringToVkMessage(string: String?): VkMessageDomain? {
        if (string == null) return null

        return try {
            val message = Gson().fromJson(string, VkMessageDomain::class.java)

            return message
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @TypeConverter
    fun fromListVkAttachmentToString(attachments: List<VkAttachment>?): String? {
        if (attachments == null) return null

        val string = attachments
            .mapNotNull(::fromVkAttachmentToString)
            .joinToString(separator = CACHE_SEPARATOR)
        return string
    }

    @TypeConverter
    fun fromStringToListVkAttachment(string: String?): List<VkAttachment>? {
        if (string == null) return null

        if (string.contains(CACHE_SEPARATOR)) {
            val attachments = string
                .split(CACHE_SEPARATOR)
                .mapNotNull(::fromStringToVkAttachment)
            return attachments
        }

        val attachment = fromStringToVkAttachment(string)

        return attachment?.let { listOf(it) }
    }

    @TypeConverter
    fun fromVkAttachmentToString(attachment: VkAttachment?): String? {
        if (attachment == null) return null

        try {
            attachment.javaClass.getDeclaredField("className")
        } catch (e: NoSuchFieldException) {
            throw AttachmentClassNameIsEmptyException(attachment)
        }
        return try {
            val string = Gson().toJson(attachment)
            string
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @TypeConverter
    fun fromStringToVkAttachment(string: String?): VkAttachment? {
        if (string.isNullOrBlank()) return null

        return try {
            val className = JSONObject(string).optString("className")

            val attachment = Gson().fromJson(string, Class.forName(className)) as? VkAttachment?

            return attachment
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class IntListToStringConverter {

    @TypeConverter
    fun fromIntListToString(list: List<Int>): String {
        return list.joinToString(separator = ",") { it.toString() }
    }

    @TypeConverter
    fun fromStringToIntList(string: String): List<Int> {
        return if (string.isEmpty()) emptyList()
        else string.split(", ").map { it.toIntOrNull() ?: -1 }
    }
}
