package com.meloda.fast.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.attachments.VkAttachment
import org.json.JSONObject
import java.util.stream.Collectors

class Converters {

    @TypeConverter
    fun fromListVkMessageToString(messages: List<VkMessage>?): String? {
        if (messages == null) return null

        val string =
            messages.map { fromVkMessageToString(it)!! }.stream()
                .collect(Collectors.joining("fastkruta228355"))

        return string
    }

    @TypeConverter
    fun fromStringToListVkMessage(string: String?): List<VkMessage>? {
        if (string == null) return null

        if (string.contains("fastkruta228355")) {
            val messages =
                string.split("fastkruta228355").map { fromStringToVkMessage(it)!! }
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

        return Gson().fromJson(string, VkMessage::class.java)
    }

    @TypeConverter
    fun fromListVkAttachmentToString(attachments: List<VkAttachment>?): String? {
        if (attachments == null) return null

        val string =
            attachments.map { fromVkAttachmentToString(it)!! }.stream()
                .collect(Collectors.joining("fastkruta228355"))

        return string
    }

    @TypeConverter
    fun fromStringToListVkAttachment(string: String?): List<VkAttachment>? {
        if (string == null) return null

        if (string.contains("fastkruta228355")) {
            val attachments =
                string.split("fastkruta228355").map { fromStringToVkAttachment(it)!! }
            return attachments
        }


        val attachment = fromStringToVkAttachment(string)!!

        return listOf(attachment)
    }

    @TypeConverter
    fun fromVkAttachmentToString(attachment: VkAttachment?): String? {
        if (attachment == null) return null

        return Gson().toJson(attachment)
    }

    @TypeConverter
    fun fromStringToVkAttachment(string: String?): VkAttachment? {
        if (string == null) return null

        val className = JSONObject(string).optString("className")

        return Gson().fromJson(string, Class.forName(className)) as VkAttachment?
    }
}
