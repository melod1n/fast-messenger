package dev.meloda.fast.model

enum class ConvoFlags(val value: Int) {
    DISABLE_PUSH(16),
    DISABLE_SOUND(32),
    INCOMING_CHAT_REQUEST(256),
    DECLINED_CHAT_REQUEST(512),
    MENTION(1024),
    HIDE_CHAT_FROM_SEARCH(2048),
    BUSINESS_CHAT(8192),
    MARKED_MESSAGE(16384), // mention or disappearing message
    DO_NOT_NOTIFY_MENTIONS_ALL_ONLINE(262144),
    DO_NOT_NOTIFY_ALL_MENTIONS(524288),
    MARKED_AS_UNREAD(1048576),
    ARCHIVED(8388608),
    CALL_IN_PROGRESS(16777216);

    companion object {

        fun parse(mask: Int): List<ConvoFlags> {
            val flags = mutableListOf<ConvoFlags>()

            ConvoFlags.entries.forEach { flag ->
                if (mask and flag.value > 0) {
                    flags.add(flag)
                }
            }

            return flags
        }
    }
}
