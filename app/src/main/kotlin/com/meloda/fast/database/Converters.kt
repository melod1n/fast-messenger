package com.meloda.fast.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.base.BaseVkMessage
import org.json.JSONObject

@Suppress("UnnecessaryVariable")
class Converters {

    private companion object {
        private const val CACHE_SEPARATOR = "fastkruta228355"
    }

    @TypeConverter
    fun fromGeoToString(geo: BaseVkMessage.Geo?): String? {
        if (geo == null) return null

        val string = Gson().toJson(geo)

        return string
    }

    @TypeConverter
    fun fromStringToGeo(string: String?): BaseVkMessage.Geo? {
        if (string == null) return null

        val geo = Gson().fromJson(string, BaseVkMessage.Geo::class.java)

        return geo
    }

    @TypeConverter
    fun fromListVkMessageToString(messages: List<VkMessage>?): String? {
        if (messages == null) return null

        val string = messages.map { fromVkMessageToString(it)!! }.joinToString { CACHE_SEPARATOR }

        return string
    }

    @TypeConverter
    fun fromStringToListVkMessage(string: String?): List<VkMessage>? {
        if (string == null) return null

        if (string.contains(CACHE_SEPARATOR)) {
            val messages =
                string.split(CACHE_SEPARATOR).map { fromStringToVkMessage(it)!! }
            return messages
        }


        val message = fromStringToVkMessage(string)!!

        return listOf(message)
    }

    @TypeConverter
    fun fromVkMessageToString(message: VkMessage?): String? {
        if (message == null) return null

        return Gson().toJson(message)
    }

    @TypeConverter
    fun fromStringToVkMessage(string: String?): VkMessage? {
        if (string == null) return null

        val message = Gson().fromJson(string, VkMessage::class.java)

        return message
    }

    @TypeConverter
    fun fromListVkAttachmentToString(attachments: List<VkAttachment>?): String? {
        if (attachments == null) return null

        val string =
            attachments.map { fromVkAttachmentToString(it)!! }.joinToString { CACHE_SEPARATOR }

        return string
    }

    @TypeConverter
    fun fromStringToListVkAttachment(string: String?): List<VkAttachment>? {
        if (string == null) return null

        if (string.contains(CACHE_SEPARATOR)) {
            val attachments =
                string.split(CACHE_SEPARATOR).map { fromStringToVkAttachment(it)!! }
            return attachments
        }


        val attachment = fromStringToVkAttachment(string)!!

        return listOf(attachment)
    }

    @TypeConverter
    fun fromVkAttachmentToString(attachment: VkAttachment?): String? {
        if (attachment == null) return null

        val string = Gson().toJson(attachment)

        return string
    }

    @TypeConverter
    fun fromStringToVkAttachment(string: String?): VkAttachment? {
        if (string == null) return null

        val className = JSONObject(string).optString("className")

        val attachment = Gson().fromJson(string, Class.forName(className)) as? VkAttachment?

        return attachment
    }
}
