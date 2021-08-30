package com.meloda.fast.api.method

import com.meloda.fast.util.ArrayUtils

class MessageMethodSetter(name: String) : MethodSetter(name) {

    fun out(value: Boolean): MessageMethodSetter {
        put("out", value)
        return this
    }

    fun timeOffset(value: Int): MessageMethodSetter {
        put("time_offset", value)
        return this
    }

    fun filters(value: Int): MessageMethodSetter {
        put("filters", value)
        return this
    }

    fun previewLength(value: Int): MessageMethodSetter {
        put("preview_length", value)
        return this
    }

    fun lastMessageId(value: Int): MessageMethodSetter {
        put("last_message_id", value)
        return this
    }

    fun unread(value: Boolean): MessageMethodSetter {
        put("unread", value)
        return this
    }

    fun messageIds(vararg ids: Int): MessageMethodSetter {
        put("message_ids", ArrayUtils.asString(ids))
        return this
    }

    fun messageIds(ids: ArrayList<Int>): MessageMethodSetter {
        put("message_ids", ArrayUtils.asString(ids))
        return this
    }

    fun q(query: String): MessageMethodSetter {
        put("q", query)
        return this
    }

    fun startMessageId(id: Int): MessageMethodSetter {
        put("start_message_id", id)
        return this
    }

    fun peerId(value: Int): MessageMethodSetter {
        put("peer_id", value)
        return this
    }

    fun peerIds(vararg values: Int): MessageMethodSetter {
        put("peer_ids", com.meloda.fast.util.ArrayUtils.asString(values))
        return this
    }

    fun reversed(value: Boolean): MessageMethodSetter {
        put("rev", value)
        return this
    }

    fun domain(value: String): MessageMethodSetter {
        put("domain", value)
        return this
    }

    fun chatId(value: Int): MessageMethodSetter {
        put("chat_id", value)
        return this
    }

    fun message(message: String): MessageMethodSetter {
        put("message", message)
        return this
    }

    fun randomId(value: Int): MessageMethodSetter {
        put("random_id", value)
        return this
    }

    fun lat(lat: Double): MessageMethodSetter {
        put("lat", lat)
        return this
    }

    fun longitude(value: Long): MessageMethodSetter {
        put("LONG", value)
        return this
    }

    fun attachment(attachments: Collection<String>): MessageMethodSetter {
        put("attachment", com.meloda.fast.util.ArrayUtils.asString(attachments))
        return this
    }

    fun attachment(vararg attachments: String): MessageMethodSetter {
        put("attachment", com.meloda.fast.util.ArrayUtils.asString(*attachments))
        return this
    }

    fun forwardMessages(ids: Collection<String>): MessageMethodSetter {
        put("forward_messages", com.meloda.fast.util.ArrayUtils.asString(ids))
        return this
    }

    fun forwardMessages(vararg ids: Int): MessageMethodSetter {
        put("forward_messages", com.meloda.fast.util.ArrayUtils.asString(ids))
        return this
    }

    fun stickerId(value: Int): MessageMethodSetter {
        put("sticker_id", value)
        return this
    }

    fun messageId(value: Int): MessageMethodSetter {
        put("message_id", value)
        return this
    }

    fun important(value: Boolean): MessageMethodSetter {
        put("important", value)
        return this
    }

    fun ts(value: Long): MessageMethodSetter {
        put("ts", value)
        return this
    }

    fun pts(value: Int): MessageMethodSetter {
        put("pts", value)
        return this
    }

    fun msgsLimit(limit: Int): MessageMethodSetter {
        put("msgs_limit", limit)
        return this
    }

    fun onlines(onlines: Boolean): MessageMethodSetter {
        put("onlines", onlines)
        return this
    }

    fun maxMsgId(id: Int): MessageMethodSetter {
        put("max_msg_id", id)
        return this
    }

    fun chatIds(vararg ids: Int): MessageMethodSetter {
        put("max_msg_id", com.meloda.fast.util.ArrayUtils.asString(ids))
        return this
    }

    fun chatIds(ids: Collection<Int>): MessageMethodSetter {
        put("max_msg_id", com.meloda.fast.util.ArrayUtils.asString(ids))
        return this
    }

    fun title(title: String): MessageMethodSetter {
        put("title", title)
        return this
    }

    fun type(typing: Boolean): MessageMethodSetter {
        if (typing) {
            put("type", "typing")
        }
        return this
    }

    fun mediaType(type: String): MessageMethodSetter {
        put("media_type", type)
        return this
    }

    fun photoSizes(value: Boolean): MessageMethodSetter {
        return put("photo_sizes", value) as MessageMethodSetter
    }

    fun filter(value: String): MessageMethodSetter {
        return put("filter", value) as MessageMethodSetter
    }

    fun extended(value: Boolean): MessageMethodSetter {
        return put("extended", value) as MessageMethodSetter
    }

    fun markConversationAsRead(asRead: Boolean): MessageMethodSetter {
        put("mark_conversation_as_read", asRead)
        return this
    }
}