package dev.meloda.fast.model

enum class MessageFlags(val value: Int) {
    UNREAD(1),
    OUTGOING(2),
    IMPORTANT(8),
    SPAM(64),
    DELETED(128),
    AUDIO_LISTENED(4096),
    FROM_GROUP_CHAT(8192),
    CANCEL_SPAM(32768),
    DELETED_FOR_ALL(131072),
    DO_NOT_SHOW_NOTIFICATION(1048576),
    MESSAGE_WITH_REPLY(2097152),
    REACTION(16777216);

    companion object {

        fun parse(mask: Int): List<MessageFlags> {
            val flags = mutableListOf<MessageFlags>()

            entries.forEach { flag ->
                if (mask and flag.value > 0) {
                    flags.add(flag)
                }
            }

            return flags
        }
    }
}
