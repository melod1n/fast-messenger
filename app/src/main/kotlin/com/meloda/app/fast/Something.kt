package com.meloda.app.fast

/*
 fun isChat() = peerType.isChat()
    fun isUser() = peerType.isUser()
    fun isGroup() = peerType.isGroup()

    fun isInUnread() = inRead - lastMessageId < 0
    fun isOutUnread() = outRead - lastMessageId < 0

    fun isUnread() = isInUnread() || isOutUnread()

    fun isAccount() = id == UserConfig.userId

    fun isPinned() = majorId > 0

    fun getUserAndGroup(
        usersMap: VkUsersMap,
        groupsMap: VkGroupsMap
    ): Pair<VkUserDomain?, VkGroupDomain?> {
        val user: VkUserDomain? = usersMap.conversationUser(this)
        val group: VkGroupDomain? = groupsMap.conversationGroup(this)

        return user to group
    }



    private fun extractTitle(): String {
        return when {
            isAccount() -> UiText.Resource(R.string.favorites)
            peerType.isChat() -> UiText.Simple(title ?: "...")
            peerType.isUser() -> {
                UiText.Simple(
                    conversationUser?.let { user ->
                        (if (AppGlobal.preferences.getBoolean(
                                SettingsKeys.KEY_USE_CONTACT_NAMES,
                                SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
                            )
                        ) VkMemoryCache.getContact(user.id)?.name else null) ?: user.fullName
                    }.orDots()
                )
            }

            peerType.isGroup() -> UiText.Simple(conversationGroup?.name ?: "...")
            else -> UiText.Simple("...")
        }.parseString(AppGlobal.Instance).orEmpty()
    }

    private fun extractUnreadCounterText(): String? {
        if (lastMessage?.isOut.isFalse && !isInUnread()) return null

        return when {
            unreadCount == 0 -> null
            unreadCount < 1000 -> unreadCount.toString()
            else -> {
                val exp = (ln(unreadCount.toDouble()) / ln(1000.0)).toInt()
                val suffix = "KMBT"[exp - 1]

                val result = unreadCount / 1000.0.pow(exp.toDouble())

                if (result.toLong().toDouble() == result) {
                    String.format("%.0f%s", result, suffix)
                } else {
                    String.format("%.1f%s", result, suffix)
                }
            }
        }
    }

    private fun extractMessage(): AnnotatedString {
        val youPrefix = UiText.Resource(R.string.you_message_prefix)
            .parseString(AppGlobal.Instance)
            .orEmpty()

        val actionMessage = VkUtils.getActionMessageText(
            message = lastMessage,
            youPrefix = youPrefix,
            messageUser = lastMessage?.user,
            messageGroup = lastMessage?.group,
            action = lastMessage?.getPreparedAction(),
            actionUser = lastMessage?.actionUser,
            actionGroup = lastMessage?.actionGroup
        )

        val attachmentIcon: UiImage? = when {
            lastMessage?.text == null -> null
            !lastMessage.forwards.isNullOrEmpty() -> {
                if (lastMessage.forwards.size == 1) {
                    UiImage.Resource(R.drawable.ic_attachment_forwarded_message)
                } else {
                    UiImage.Resource(R.drawable.ic_attachment_forwarded_messages)
                }
            }

            else -> VkUtils.getAttachmentConversationIcon(lastMessage)
        }

        val attachmentText = (if (attachmentIcon == null) VkUtils.getAttachmentText(
            message = lastMessage
        ) else null)

        val forwardsMessage = (if (lastMessage?.text == null) VkUtils.getForwardsText(
            message = lastMessage
        ) else null)

        val messageText = lastMessage?.text.orEmpty()


        val prefixText: AnnotatedString? = when {
            actionMessage != null -> null

            lastMessage == null -> null

            id == UserConfig.userId -> null

            !peerType.isChat() && !lastMessage.isOut -> null

            lastMessage.isOut -> buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(youPrefix)
                }
            }

            else ->
                when {
                    lastMessage.user?.firstName.orEmpty().isNotEmpty() -> buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(lastMessage.user?.firstName)
                        }
                    }

                    lastMessage.group?.name.orEmpty().isNotEmpty() -> buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(lastMessage.group?.name)
                        }
                    }

                    else -> null
                }
        }

        val prefix = buildAnnotatedString {
            if (prefixText != null) {
                append(prefixText)
                append(": ")
            }
        }

        val finalText = when {
            actionMessage != null -> {
                prefix + actionMessage
            }

            forwardsMessage != null -> {
                prefix + forwardsMessage
            }

            attachmentText != null -> {
                prefix + attachmentText
            }

            else ->
                messageText
                    .let { text -> VkUtils.prepareMessageText(text, true) }
                    .let { text -> VkUtils.getTextWithVisualizedMentions(text, Color.Red) }
                    .let { text -> prefix + text }
        }

        return finalText
    }

    private fun extractAttachmentImage(): UiImage? {
        if (lastMessage?.text == null) return null
        return VkUtils.getAttachmentConversationIcon(lastMessage)
    }

    private fun extractReadCondition(): Boolean {
        return (lastMessage?.isOut.isTrue && isOutUnread()) ||
                (lastMessage?.isOut.isFalse && isInUnread())
    }

    private fun extractDate(): String {
        return TimeUtils.getLocalizedTime(AppGlobal.Instance, (lastMessage?.date ?: -1) * 1000L)
    }

    private fun extractInteractionUsers(
        usersMap: VkUsersMap,
        groupsMap: VkGroupsMap
    ): List<String> {
        return interactionIds.mapNotNull { id ->
            when {
                id > 0 -> usersMap.user(id)?.fullName
                id < 0 -> groupsMap.group(id)?.name
                else -> null
            }
        }
    }

    private fun extractBirthday(): Boolean {
        val birthday = conversationUser?.birthday ?: return false
        val splitBirthday = birthday.split(".").mapNotNull(String::toIntOrNull)

        if (splitBirthday.isEmpty()) return false

        return if (splitBirthday.size > 1) {
            val (day, month) = splitBirthday

            val birthdayCalendar = Calendar.getInstance().also { calendar ->
                calendar.dayOfMonth = day
                calendar.month = month - 1
            }

            val nowCalendar = Calendar.getInstance()

            nowCalendar.dayOfMonth == birthdayCalendar.dayOfMonth &&
                    nowCalendar.month == birthdayCalendar.month
        } else false
    }

    private fun extractInteractionText(
        usersMap: VkUsersMap,
        groupsMap: VkGroupsMap
    ): String? {
        val interactionType = InteractionType.parse(interactionType)
        val interactiveUsers = extractInteractionUsers(usersMap = usersMap, groupsMap = groupsMap)

        val typingText =
            if (interactionType == null) {
                null
            } else {
                if (!peerType.isChat() && interactiveUsers.size == 1) {
                    when (interactionType) {
                        InteractionType.File -> R.string.chat_interaction_uploading_file
                        InteractionType.Photo -> R.string.chat_interaction_uploading_photo
                        InteractionType.Typing -> R.string.chat_interaction_typing
                        InteractionType.Video -> R.string.chat_interaction_uploading_video
                        InteractionType.VoiceMessage -> R.string.chat_interaction_recording_audio_message
                    }.let(UiText::Resource)
                } else {
                    if (interactiveUsers.size == 1) {
                        R.string.chat_interaction_chat_single_typing
                    } else {
                        R.string.chat_interaction_chat_typing
                    }.let { resId ->
                        UiText.ResourceParams(
                            resId,
                            listOf(interactiveUsers.joinToString(separator = ", "))
                        )
                    }
                }.parseString(AppGlobal.Instance)
            }

        return typingText
    }

    private fun extractLastSeenStatus(): String? {
        return when {
            isChat() -> {
                membersCount?.let { count -> "Members: $count" }.orDots()
            }

            isGroup() -> {
                conversationGroup?.membersCount?.let { count -> "Members: $count" }.orDots()
            }

            isUser() -> {
                conversationUser?.lastSeen?.let { time ->
                    TimeUtils.getLocalizedDate(AppGlobal.Instance, time * 1000L)
                }.orDots()
            }

            else -> null
        }
    }

    fun mapToPresentation(
        usersMap: VkUsersMap,
        groupsMap: VkGroupsMap
    ): VkConversationUi = VkConversationUi(
        id = id,
        lastMessageId = lastMessageId,
        avatar = extractAvatar(),
        title = extractTitle(),
        unreadCount = extractUnreadCounterText(),
        date = extractDate(),
        message = extractMessage(),
        attachmentImage = extractAttachmentImage(),
        isPinned = majorId > 0,
        actionImageId = ActionState.parse(isPhantom, isCallInProgress).getResourceId(),
        isBirthday = extractBirthday(),
        isUnread = extractReadCondition(),
        isAccount = isAccount(),
        isOnline = !isAccount() && conversationUser?.onlineStatus?.isOnline() == true,
        lastMessage = lastMessage,
        conversationUser = conversationUser,
        conversationGroup = conversationGroup,
        peerType = peerType,
        interactionText = extractInteractionText(usersMap = usersMap, groupsMap = groupsMap),
        isExpanded = false,
        options = ImmutableList.of(),
        lastSeenStatus = extractLastSeenStatus()
    )


 */
