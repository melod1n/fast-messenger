package com.meloda.fast.screens.messages

import android.animation.ValueAnimator
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.github.terrakok.cicerone.Router
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.net.MediaType
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.attachments.VkPhoto
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.databinding.DialogMessageDeleteBinding
import com.meloda.fast.databinding.FragmentMessagesHistoryBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.clear
import com.meloda.fast.ext.doOnApplyWindowInsets
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.getParcelableCompat
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.hideKeyboard
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.mimeType
import com.meloda.fast.ext.orDots
import com.meloda.fast.ext.sdk30AndUp
import com.meloda.fast.ext.selectLast
import com.meloda.fast.ext.showKeyboard
import com.meloda.fast.ext.trimmedText
import com.meloda.fast.ext.visible
import com.meloda.fast.model.base.parseString
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.ColorUtils
import com.meloda.fast.util.ShareContent
import com.meloda.fast.util.TimeUtils
import com.meloda.fast.view.SpaceItemDecoration
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.abs
import kotlin.properties.Delegates
import kotlin.random.Random

class MessagesHistoryFragment :
    BaseViewModelFragment<MessagesHistoryViewModel>(R.layout.fragment_messages_history) {

    private val router: Router by inject()

    private val binding by viewBinding(FragmentMessagesHistoryBinding::bind)
    override val viewModel: MessagesHistoryViewModel by viewModel()

    private var pickFile: Boolean = false

    private val attachmentsToLoad = mutableListOf<VkAttachment>()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList: List<Uri>? ->
            if (uriList.isNullOrEmpty()) {
                return@registerForActivityResult
            }

            if (uriList.size > 10) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.warning)
                    .setMessage("Select no more than 10 files")
                    .setPositiveButton(R.string.ok, null)
                    .show()
                return@registerForActivityResult
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val uploadFlow = flow<Any?> {
                    uriList.forEach { uri ->
                        processFileFromStorage(uri)
                        emit(null)
                    }
                }

                uploadFlow.collect()
            }
        }


    private val actionState = MutableStateFlow(Action.RECORD)

    private enum class Action {
        RECORD, SEND, EDIT, DELETE
    }

    private val user: VkUser? by lazy {
        requireArguments().getParcelableCompat(ARG_USER, VkUser::class.java)
    }

    private val group: VkGroup? by lazy {
        requireArguments().getParcelableCompat(ARG_GROUP, VkGroup::class.java)
    }

    private var conversation: VkConversationDomain by Delegates.notNull()

    private val adapter: MessagesHistoryAdapter by lazy {
        MessagesHistoryAdapter(this, conversation).also {
            it.itemClickListener = this::onItemClick
            it.avatarLongClickListener = this::onAvatarLongClickListener
        }
    }

    private val attachmentsAdapter: AttachmentsAdapter by lazy {
        AttachmentsAdapter(
            requireContext(),
            emptyList(),
            onRemoveClickedListener = { position ->
                removeAttachment(attachmentsAdapter[position])
            }
        )
    }

    private var timestampTimer: Timer? = null

    private lateinit var attachmentController: AttachmentPanelController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversation = requireNotNull(
            requireArguments().getParcelableCompat(
                ARG_CONVERSATION,
                VkConversationDomain::class.java
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorBackground = ContextCompat.getColor(requireContext(), R.color.colorBackground)
        val alphaColorBackground = ColorUtils.alphaColor(colorBackground, 0.85F)
        binding.bottomMessagePanel.setBackgroundColor(alphaColorBackground)

        binding.toolbar.startButtonClickAction = {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        attachmentController = AttachmentPanelController.init(
            context = requireContext(),
            adapter = adapter,
            lifecycleOwner = viewLifecycleOwner,
            binding = binding,
            isAttachmentsEmpty = { attachmentsToLoad.isEmpty() }
        )

        val title = when {
            conversation.isChat() -> conversation.conversationTitle
            conversation.isUser() -> user?.toString()
            conversation.isGroup() -> group?.name
            else -> null
        }

//        listOf(
//            binding.bottomAlpha,
//            binding.bottomGradient
//        ).forEach { v ->
//            v.applyInsetter {
//                type(navigationBars = true) { padding() }
//            }
//        }
        binding.bottomMessagePanel.applyInsetter {
            type(navigationBars = true, ime = true) { padding(animated = true) }
        }
//        binding.recyclerView.applyInsetter {
//            type(navigationBars = true, ime = true) { padding(animated = true) }
//        }
        binding.toolbar.applyInsetter {
            type(statusBars = true) { padding() }
        }
        binding.toolbar.title = title.orDots()
        binding.toolbar.setOnClickListener {
            openChatInfoScreen(conversation, user, group)
        }

        val status = when {
            conversation.isChat() -> "${conversation.membersCount} members"
            conversation.isUser() -> when {
                // TODO: 9/15/2021 user normal time
                user?.online == true -> "Online"
                user?.lastSeen != null -> "Last seen at ${
                    SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                    ).format(user?.lastSeen!! * 1000L)
                }"

                else -> if (user?.lastSeenStatus != null) "Last seen ${user?.lastSeenStatus!!}" else "Last seen recently"
            }

            conversation.isGroup() -> if (group?.membersCount != null) "${group?.membersCount} members" else "Group"
            else -> null
        }

        binding.toolbar.subtitle = status.orDots()

        prepareAvatar()

        prepareViews()

        binding.recyclerView.adapter = adapter

        viewModel.loadHistory(conversation.id)

        binding.action.setOnClickListener {
            performAction()
        }

//        binding.recyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
//            if (bottom >= oldBottom) return@addOnLayoutChangeListener
//            checkIfNeedToScrollToBottom()
//        }

        binding.unreadCounter.setOnClickListener {
            binding.recyclerView.scrollToPosition(adapter.lastPosition)
        }

        binding.recyclerView.setItemViewCacheSize(30)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                if (AppGlobal.preferences.getBoolean(
                        SettingsFragment.KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL,
                        true
                    ) && dy < 0
                ) {
                    binding.recyclerView.hideKeyboard()
                }

                setUnreadCounterVisibility(lastPosition, dy)

                adapter.getOrNull(firstPosition)?.let {
                    binding.timestamp.visible()

                    val showExactTime = AppGlobal.preferences.getBoolean(SettingsFragment.KEY_SHOW_EXACT_TIME_ON_TIME_STAMP, false)

                    val exactTime = SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                    ).format(it.date * 1000L)

                    val time = "${
                        TimeUtils.getLocalizedDate(
                            requireContext(),
                            it.date * 1000L
                        )
                    }${if (showExactTime) ", $exactTime" else ""}"

                    binding.timestamp.text = time

                    if (timestampTimer != null) {
                        timestampTimer?.cancel()
                        timestampTimer = null
                    }

                    timestampTimer = Timer()
                    timestampTimer?.schedule(2500) {
                        recyclerView.post {
                            if (getView() == null) return@post
                            binding.timestamp.gone()
                        }
                    }
                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })

        binding.message.doAfterTextChanged { text ->
            val canSend = text.toString().isNotBlank() || attachmentsToLoad.isNotEmpty()

            val newValue: Action =
                when {
                    attachmentController.isEditing ->
                        if (text.isNullOrBlank() && attachmentsToLoad.isEmpty()) {
                            Action.DELETE
                        } else {
                            Action.EDIT
                        }

                    canSend -> Action.SEND
                    else -> {
                        Action.RECORD
                    }
                }

            actionState.update { newValue }
        }

        actionState
            .asStateFlow()
            .flowWithLifecycle(lifecycle)
            .onEach { state ->
                binding.action.animate()
                    .scaleX(1.25f)
                    .scaleY(1.25f)
                    .setDuration(100)
                    .withEndAction {
                        if (getView() == null) return@withEndAction

                        binding.action.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }.start()

                when (state) {
                    Action.RECORD -> {
                        binding.action.setImageResource(R.drawable.ic_round_mic_24)
                    }

                    Action.SEND -> {
                        binding.action.setImageResource(R.drawable.ic_round_send_24)
                    }

                    Action.EDIT -> {
                        binding.action.setImageResource(R.drawable.ic_round_done_24)
                    }

                    Action.DELETE -> {
                        binding.action.setImageResource(R.drawable.ic_trash_can_outline_24)
                    }
                }
            }
            .launchIn(lifecycleScope)

        attachmentController.isPanelVisible.listenValue { isVisible ->
            if (isVisible) binding.message.setSelection(binding.message.text.toString().length)

//            val currentHeight = binding.listAnchor.height
//
//            val newHeight =
//                if (isVisible) (binding.attachmentPanel.measuredHeight / 1.5).roundToInt()
//                else 1
//
//            ValueAnimator.ofInt(currentHeight, newHeight).apply {
//                duration = ATTACHMENT_PANEL_ANIMATION_DURATION
//                interpolator = LinearInterpolator()
//
//                addUpdateListener { animator ->
//                    if (getView() == null) return@addUpdateListener
//                    val value = animator.animatedValue as Int
//
//                    binding.listAnchor.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                        height = value
//                    }
//                }
//            }.start()
        }

        binding.replyMessage.setOnClickListener {
            val message = attachmentController.message.value ?: return@setOnClickListener
            val index = adapter.searchMessageIndex(message.id) ?: return@setOnClickListener

            binding.recyclerView.scrollToPosition(index)
        }

        binding.dismissReply.setOnClickListener {
            if (attachmentController.message.value != null)
                attachmentController.message.update { null }
        }

        binding.attach.setOnClickListener {
            showAttachmentsPopupMenu()
        }

        binding.attach.setOnLongClickListener {
            pickPhoto()
            true
        }
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            is MessagesMarkAsImportantEvent -> markMessagesAsImportant(event)
            is MessagesLoadedEvent -> refreshMessages(event)
            is MessagesPinEvent -> conversation.pinnedMessage = event.message
            is MessagesUnpinEvent -> conversation.pinnedMessage = null
            is MessagesDeleteEvent -> deleteMessages(event)
            is MessagesEditEvent -> editMessage(event)
            is MessagesReadEvent -> readMessages(event)
            is MessagesNewEvent -> addNewMessage(event)
        }
    }

    private fun checkIfNeedToScrollToBottom() {
        if (adapter.isEmpty()) return

        val lastVisiblePosition =
            (binding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        if (lastVisiblePosition <= adapter.lastPosition - 10) return

        binding.recyclerView.postDelayed({
            if (view == null) return@postDelayed
            binding.recyclerView.smoothScrollToPosition(adapter.lastPosition)
        }, 0)
    }

    private suspend fun processFileFromStorage(uri: Uri) {
        var name = ""
        var size = 0.0

        val contentResolver = requireContext().contentResolver
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            cursor.moveToFirst()
            name = cursor.getString(nameIndex)
            size = AndroidUtils.bytesToMegabytes(cursor.getLong(sizeIndex).toDouble())
            cursor.close()
        }

        if (size > 200) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.warning)
                .setMessage("Selected file weighs more than 200 megabytes. Compress it or send other file")
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false)
                .show()
            return
        }

        val lastDotIndex = name.lastIndexOf(".")
        var extension = if (lastDotIndex == -1) "" else name.substring(lastDotIndex + 1)

        if (extension.endsWith("msi") || extension.endsWith("exe") || extension.endsWith("apk")) {
            extension += "fast"
            name += "fast"
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.warning)
                .setMessage("Selected file is executable. Fast changed it extension to \"$extension\", so the final name is \"$name\"")
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false)
                .show()
        }

        val destination = requireContext()
            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() +
                "${File.separator}upload.$extension"

        val file = File(destination)
        if (file.exists()) file.delete()

        withContext(Dispatchers.IO) {
            val inputStream =
                requireActivity().contentResolver.openInputStream(uri) ?: return@withContext

            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        val mimeType = contentResolver.getType(uri) ?: return

        if (pickFile) {
            val uploadedAttachment = viewModel.uploadFile(
                conversation.id,
                file,
                name,
                FilesRepository.FileType.File
            )
            addAttachment(uploadedAttachment)
        } else {
            when (MediaType.parse(mimeType).type()) {
                MediaType.ANY_IMAGE_TYPE.type() -> {
                    val uploadedAttachment = viewModel.uploadPhoto(conversation.id, file, name)
                    addAttachment(uploadedAttachment)
                }

                MediaType.ANY_VIDEO_TYPE.type() -> {
                    val uploadedAttachment = viewModel.uploadVideo(file, name)
                    addAttachment(uploadedAttachment)
                }

                MediaType.ANY_AUDIO_TYPE.type() -> {
                    val uploadedAttachment = viewModel.uploadAudio(file, name)
                    addAttachment(uploadedAttachment)
                }
            }
        }
    }

    private fun showAttachmentsPopupMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.attach)

        if (attachmentsToLoad.isNotEmpty()) {
            popupMenu.menu.add("Clear attachments")
        }

        popupMenu.menu.add("Photo")
        popupMenu.menu.add("Video")
        popupMenu.menu.add("Audio")
        popupMenu.menu.add("File")
        popupMenu.setOnMenuItemClickListener { menuItem ->
            return@setOnMenuItemClickListener when (menuItem.title) {
                "Clear attachments" -> {
                    clearAttachments()
                    true
                }

                "Photo" -> {
                    pickPhoto()
                    true
                }

                "Video" -> {
                    pickVideo()
                    true
                }

                "Audio" -> {
                    pickAudio()
                    true
                }

                "File" -> {
                    pickFile()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun addAttachment(attachment: VkAttachment) {
        attachmentsToLoad += attachment
        binding.attachmentsCounter.visible()
        binding.attachmentsCounter.text = attachmentsToLoad.size.toString()

        binding.attachmentsList.visible()
        attachmentsAdapter.add(attachment)

        attachmentController.showPanel()

        actionState.value =
            if (attachmentController.isEditing) Action.EDIT
            else Action.SEND
    }

    private fun removeAttachment(attachment: VkAttachment) {
        attachmentsToLoad -= attachment
        binding.attachmentsCounter.visible()
        binding.attachmentsCounter.text = attachmentsToLoad.size.toString()

        binding.attachmentsList.visible()

        attachmentController.showPanel()

        if (attachmentsToLoad.isEmpty()) {
            clearAttachments()
        } else {
            attachmentsAdapter.remove(attachment)
        }
    }

    private fun clearAttachments() {
        attachmentsToLoad.clear()
        binding.attachmentsCounter.gone()
        binding.attachmentsCounter.text = null

        attachmentsAdapter.clear()
        binding.attachmentsList.gone()

        attachmentController.hidePanel()
    }

    private fun pickPhoto() {
        getContent.launch(MediaType.ANY_IMAGE_TYPE.mimeType)
    }

    private fun pickVideo() {
        getContent.launch(MediaType.ANY_VIDEO_TYPE.mimeType)
    }

    private fun pickAudio() {
        getContent.launch(MediaType.MPEG_AUDIO.mimeType)
    }

    private fun pickFile() {
        pickFile = true
        getContent.launch(MediaType.ANY_TYPE.mimeType)
    }

    fun scrollToMessage(messageId: Int) {
        adapter.searchMessageIndex(messageId)?.let { index ->
            binding.recyclerView.scrollToPosition(index)
        }
    }

    private fun prepareAvatar() {
        val avatar = when {
            conversation.isUser() -> user?.photo200
            conversation.isGroup() -> group?.photo200
            conversation.isChat() -> conversation.conversationPhoto
            else -> null
        }

        val avatarImageView = binding.toolbar.avatarImageView
        avatarImageView.visible()
        avatarImageView.loadWithGlide {
            imageUrl = avatar
            asCircle = true
            crossFade = true
        }
    }

    private fun performAction() {
        when (actionState.value) {
            Action.RECORD -> {
                sdk30AndUp {
                    binding.action.performHapticFeedback(HapticFeedbackConstants.REJECT)
                }
            }

            Action.SEND -> {
                val messageText = binding.message.trimmedText
                if (messageText.isBlank() && attachmentsToLoad.isEmpty()) {
                    Log.d(
                        "MessagesHistoryFragment",
                        "performAction: SEND: messageText is empty & attachments is empty. return"
                    )
                    return
                }

                val date = System.currentTimeMillis()

                val messageIndex = adapter.lastPosition

                val attachments = attachmentsToLoad.ifEmpty { null }?.toList()
                clearAttachments()

                val message = VkMessage(
                    id = Int.MAX_VALUE,
                    text = messageText,
                    isOut = true,
                    peerId = conversation.id,
                    fromId = UserConfig.userId,
                    date = (date / 1000).toInt(),
                    randomId = Random.nextInt(),
                    replyMessage = attachmentController.message.value,
                    attachments = attachments,
                ).also {
                    it.state = VkMessage.State.Sending
                }

                Log.d("LongPollUpdatesParser", "newMessageRandomId: ${message.randomId}")

                adapter.add(message, commitCallback = {
                    binding.recyclerView.scrollToPosition(adapter.lastPosition)
                    binding.message.clear()
                })

                val replyMessage = attachmentController.message.value
                attachmentController.message.update { null }

                sdk30AndUp {
                    binding.action.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                }

                viewModel.sendMessage(
                    peerId = conversation.id,
                    message = messageText.ifBlank { null },
                    randomId = message.randomId,
                    replyTo = replyMessage?.id,
                    setId = { messageId ->
                        val messageToUpdate = adapter[messageIndex]
                        messageToUpdate.id = messageId
                        messageToUpdate.state = VkMessage.State.Sent
                        adapter.notifyItemChanged(messageIndex, "kek")
//                        adapter[messageIndex] = messageToUpdate
                        attachmentsAdapter.clear()
                    },
                    onError = {
                        val messageToUpdate = adapter[messageIndex]
                        messageToUpdate.state = VkMessage.State.Error
                        adapter.notifyItemChanged(messageIndex, "kek")
//                        adapter[messageIndex] = messageToUpdate
                        attachmentsAdapter.clear()
                    },
                    attachments = attachments
                )
            }

            Action.EDIT -> {
                val message = attachmentController.message.value ?: return
                val messageText = binding.message.text.toString().trim()

                attachmentController.message.update { null }

                viewModel.editMessage(
                    originalMessage = message,
                    peerId = conversation.id,
                    messageId = message.id,
                    message = messageText,
                    attachments = message.attachments
                )
            }

            Action.DELETE -> attachmentController.message.value?.let {
                showDeleteMessageDialog(it)
            }
        }
    }

    private fun prepareViews() {
        prepareRecyclerView()
        prepareEmojiButton()
        prepareAttachmentsList()
    }

    private fun prepareRecyclerView() {
        binding.recyclerView.itemAnimator = null

        binding.toolbar.measure(
            View.MeasureSpec.AT_MOST,
            View.MeasureSpec.UNSPECIFIED
        )

        binding.bottomMessagePanel.measure(
            View.MeasureSpec.AT_MOST,
            View.MeasureSpec.UNSPECIFIED
        )

//        binding.recyclerView.updatePaddingRelative(
//            top = binding.toolbar.measuredHeight,
//            bottom = binding.bottomMessagePanel.measuredHeight
//        )

        val toolbarMeasuredHeight = binding.toolbar.measuredHeight
        val bottomMessagePanelMeasuredHeight = binding.bottomMessagePanel.measuredHeight

        binding.recyclerView.doOnApplyWindowInsets { v, insets, _, _ ->
            val statusBars = AndroidUtils.getStatusBarInsets(insets)
            val ime = AndroidUtils.getImeInsets(insets)
            val navBars = AndroidUtils.getNavBarInsets(insets)

            val topPadding = toolbarMeasuredHeight + statusBars.top

            val bottomPadding = bottomMessagePanelMeasuredHeight +
                    ime.bottom + (if (ime.bottom == 0) navBars.bottom else 0)

            val currentPadding = v.paddingBottom

            v.updatePaddingRelative(top = topPadding)
            ValueAnimator.ofInt(currentPadding, bottomPadding).apply {
                interpolator = LinearInterpolator()
                duration = if (currentPadding > bottomPadding) 125 else 50

                addUpdateListener {
                    if (view == null) return@addUpdateListener
                    val value = it.animatedValue as Int
                    v.updatePaddingRelative(bottom = value)
                }

                doOnEnd {
                    if (view == null) return@doOnEnd
                    checkIfNeedToScrollToBottom()
                }
            }.start()

//            v.updatePaddingRelative(top = topPadding, bottom = bottomPadding)

            insets
        }
    }

    private fun prepareEmojiButton() {
        binding.emoji.setOnClickListener {
            sdk30AndUp {
                binding.emoji.performHapticFeedback(HapticFeedbackConstants.REJECT)
            }
        }
        binding.emoji.setOnLongClickListener {
            val text = binding.message.text.toString() +
                    AppGlobal.preferences.getString(
                        SettingsFragment.KEY_FEATURES_FAST_TEXT,
                        SettingsFragment.DEFAULT_VALUE_FEATURES_FAST_TEXT
                    )
            binding.message.setText(text)
            binding.message.selectLast()

            binding.emoji.animate()
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(100)
                .withEndAction {
                    if (view == null) return@withEndAction

                    binding.emoji.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }.start()

            binding.emoji.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        }
    }

    private fun prepareAttachmentsList() {
        binding.attachmentsList.addItemDecoration(
            SpaceItemDecoration(endMargin = 4.dpToPx())
        )
        binding.attachmentsList.adapter = attachmentsAdapter
    }

    private fun markMessagesAsImportant(event: MessagesMarkAsImportantEvent) {
        val newList = adapter.cloneCurrentList()

        for (i in newList.indices) {
            val message = newList[i]
            if (event.messagesIds.contains(message.id)) {
                newList[i] = message.copy(important = event.important)
            }
        }

        adapter.submitList(newList)
    }

    private fun refreshMessages(event: MessagesLoadedEvent) {
        adapter.profiles += event.profiles
        adapter.groups += event.groups

        fillRecyclerView(event.messages)
    }

    private fun fillRecyclerView(values: List<VkMessage>) {
        val smoothScroll = adapter.isNotEmpty()

        adapter.setItems(
            values.sortedBy { it.date },
            commitCallback = {
                if (view == null) return@setItems
                if (smoothScroll) binding.recyclerView.smoothScrollToPosition(adapter.lastPosition)
                else binding.recyclerView.scrollToPosition(adapter.lastPosition)
            }
        )
    }

    private fun onItemClick(position: Int) {
        showOptionsDialog(position)
    }

    private fun onAvatarLongClickListener(position: Int) {
        val message = adapter[position]

        val messageUser = VkUtils.getMessageUser(message, adapter.profiles)
        val messageGroup = VkUtils.getMessageGroup(message, adapter.groups)

        val title = VkUtils.getMessageTitle(message, messageUser, messageGroup)
        Toast.makeText(requireContext(), title, Toast.LENGTH_SHORT).show()
    }

    private fun showOptionsDialog(position: Int) {
        val message = adapter[position]
        if (message.action != null) return

        val time = getString(
            R.string.time_format,
            SimpleDateFormat(
                "dd.MM.yyyy, HH:mm:ss",
                Locale.getDefault()
            ).format(message.date * 1000L)
        )

        val reply = getString(R.string.message_context_action_reply)

        val isMessageAlreadyPinned = message.id == conversation.pinnedMessage?.id

        val pin = getString(
            if (isMessageAlreadyPinned) R.string.message_context_action_unpin
            else R.string.message_context_action_pin
        )

        val important = getString(
            if (message.important) R.string.message_context_action_unmark_as_important
            else R.string.message_context_action_mark_as_important
        )

        val read = "Mark as read"

        val edit = getString(R.string.message_context_action_edit)

        val copy = "Copy"

        val share = "Share"

        val delete = getString(R.string.message_context_action_delete)

        val params = mutableListOf<String>()
        val onlySentParams = mutableListOf<String>()

        params += reply
        onlySentParams += reply

        if (conversation.canChangePin) {
            params += pin
            onlySentParams += pin
        }

        params += important
        onlySentParams += important

        if (!message.isRead(conversation) && !message.isOut) {
            params += read
            onlySentParams += read
        }

        if (message.canEdit()) {
            params += edit
            onlySentParams += edit
        }

        val notNullText = message.text.orEmpty()
        val messageTextIsNotNull = !message.text.isNullOrBlank()

        val notNullAttachments = message.attachments.orEmpty()
        val attachmentsIsOnePhoto = notNullAttachments.size == 1 &&
                notNullAttachments.first() is VkPhoto

        if (messageTextIsNotNull || attachmentsIsOnePhoto) {
            params += copy
            params += share
        }

        params += delete

        if (!message.isSent()) {
            params.removeAll(onlySentParams)
        }

        val arrayParams = params.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(time)
            .setItems(arrayParams) { _, which ->
                when (params[which]) {
                    reply -> {
                        if (attachmentController.message.value != message)
                            attachmentController.message.update { message }
                    }

                    pin -> showPinMessageDialog(
                        peerId = conversation.id,
                        messageId = message.id,
                        pin = !isMessageAlreadyPinned
                    )

                    important -> viewModel.markAsImportant(
                        messagesIds = listOf(message.id),
                        important = !message.important
                    )

                    read -> viewModel.readMessage(
                        conversation.id,
                        message.id
                    )

                    edit -> {
                        attachmentController.isEditing = true

                        if (attachmentController.message.value != message)
                            attachmentController.message.update { message }
                    }

                    copy -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            when {
                                messageTextIsNotNull && !attachmentsIsOnePhoto -> {
                                    withContext(Dispatchers.Main) {
                                        AndroidUtils.copyText(
                                            text = notNullText,
                                            withToast = true
                                        )
                                    }
                                }

                                else -> {
                                    val imageUrl =
                                        ((notNullAttachments.first() as? VkPhoto)?.getMaxSize()
                                            ?: return@launch).url

                                    val preloadedImageFileUri = Glide
                                        .with(requireContext())
                                        .downloadOnly()
                                        .load(imageUrl)
                                        .submit()
                                        .get().let { file ->
                                            val newFile =
                                                AndroidUtils.getImageToShare(requireContext(), file)

                                            newFile!!
                                        }

                                    withContext(Dispatchers.Main) {
                                        if (messageTextIsNotNull) {
                                            AndroidUtils.copyText(text = notNullText)
                                            AndroidUtils.copyImage(
                                                label = "Image",
                                                imageUri = preloadedImageFileUri,
                                                withToast = true
                                            )
                                        } else {
                                            AndroidUtils.copyImage(
                                                label = "Image",
                                                imageUri = preloadedImageFileUri,
                                                withToast = true
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    share -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val content = when {
                                messageTextIsNotNull && !attachmentsIsOnePhoto -> {
                                    ShareContent.Text(notNullText)
                                }

                                else -> {
                                    val imageUrl =
                                        ((notNullAttachments.first() as? VkPhoto)?.getMaxSize()
                                            ?: return@launch).url

                                    val preloadedImageFileUri = Glide
                                        .with(requireContext())
                                        .downloadOnly()
                                        .load(imageUrl)
                                        .submit()
                                        .get().let { file ->
                                            val newFile =
                                                AndroidUtils.getImageToShare(requireContext(), file)

                                            newFile!!
                                        }

                                    if (messageTextIsNotNull) {
                                        ShareContent.TextWithImage(
                                            notNullText, preloadedImageFileUri
                                        )
                                    } else {
                                        ShareContent.Image(preloadedImageFileUri)
                                    }
                                }
                            }

                            withContext(Dispatchers.Main) {
                                AndroidUtils.showShareSheet(requireActivity(), content)
                            }
                        }
                    }

                    delete -> showDeleteMessageDialog(message)
                }
            }.show()
    }

    private fun showPinMessageDialog(
        peerId: Int,
        messageId: Int?,
        pin: Boolean,
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                if (pin) R.string.confirm_pin_message
                else R.string.confirm_unpin_message
            )
            .setPositiveButton(
                if (pin) R.string.action_pin
                else R.string.action_unpin
            ) { _, _ ->
                viewModel.pinMessage(
                    peerId = peerId,
                    messageId = messageId,
                    pin = pin
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteMessageDialog(message: VkMessage) {
        val binding = DialogMessageDeleteBinding.inflate(layoutInflater, null, false)

        binding.check.setText(
            if (message.isOut || conversation.canChangeInfo) R.string.message_delete_for_all
            else R.string.message_mark_as_spam
        )

        binding.check.isEnabled =
            message.isSent() && ((conversation.id != UserConfig.userId) && (!message.isOut || message.canEdit()))

        if (message.isSent() && conversation.id == UserConfig.userId ||
            (binding.check.isEnabled && message.isOut)
        ) binding.check.isChecked = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete_message)
            .setView(binding.root)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                attachmentController.message.update { null }

                if (message.isError()) {
                    adapter.searchIndexOf(message)?.let { index ->
                        adapter.removeAt(index)
                    }

                    return@setPositiveButton
                }

                viewModel.deleteMessage(
                    peerId = conversation.id,
                    messagesIds = listOf(message.id),
                    isSpam = if (message.isOut) null else binding.check.isChecked,
                    deleteForAll = if (!binding.check.isEnabled) null else binding.check.isChecked
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteMessages(event: MessagesDeleteEvent) {
        if (event.peerId != conversation.id) return
        val messagesToDelete =
            event.messagesIds.mapNotNull { id -> adapter.searchMessageById(id) }
        adapter.removeAll(messagesToDelete)
    }

    private fun editMessage(event: MessagesEditEvent) {
        if (event.message.peerId != conversation.id) return
        adapter.searchMessageIndex(event.message.id)?.let { index ->
            adapter[index] = event.message
            adapter.notifyItemChanged(index)
        }
    }

    private fun readMessages(event: MessagesReadEvent) {
        if (event.peerId != conversation.id) return

        val oldOutRead = conversation.outRead
        val oldInRead = conversation.inRead

        conversation = conversation.copy(
            outRead = if (event.isOut) event.messageId else conversation.outRead,
            inRead = if (!event.isOut) event.messageId else conversation.inRead
        )

        val positionsToUpdate = mutableListOf<Int>()
        val newList = adapter.cloneCurrentList()
        for (i in newList.indices) {
            val message = newList[i]

            if ((message.isOut && conversation.outRead - oldOutRead > 0 && message.id > oldOutRead) ||
                (!message.isOut && conversation.inRead - oldInRead > 0 && message.id > oldInRead)
            ) {
                positionsToUpdate += i
            }
        }

        positionsToUpdate.forEach { index ->
            adapter.notifyItemChanged(index)

            if (binding.unreadCounter.isVisible) {
                setUnreadCounterVisibility(
                    (binding.recyclerView.layoutManager as LinearLayoutManager)
                        .findLastCompletelyVisibleItemPosition()
                )
            }
        }
    }

    private fun setUnreadCounterVisibility(
        lastCompletelyVisiblePosition: Int,
        dy: Int? = null,
    ) {
        if (lastCompletelyVisiblePosition >= adapter.lastPosition - 1) {
            setUnreadCounterVisibility(false)
        } else {
            if (adapter.containsUnreadMessages()) {
                setUnreadCounterVisibility(true)
            } else {
                if (dy == null) {
                    setUnreadCounterVisibility(false)
                } else {
                    if (dy > 0) {
                        if (dy > 40) setUnreadCounterVisibility(true)
                    } else {
                        if (dy < -40) setUnreadCounterVisibility(false)
                    }
                }
            }
        }
    }

    private fun addNewMessage(event: MessagesNewEvent) {
        if (event.message.peerId != conversation.id) return

        adapter.profiles += event.profiles
        adapter.groups += event.groups

        if (adapter.containsRandomId(event.message.randomId)
            || adapter.containsId(event.message.id)
        ) return

        val itemCount = adapter.itemCount

        adapter.add(event.message) {
            if (view == null) return@add

            val lastVisiblePosition =
                (binding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            if (abs(lastVisiblePosition - adapter.lastPosition) <= 3) {
                binding.recyclerView.scrollToPosition(adapter.lastPosition)
            } else {
                setUnreadCounterVisibility(true)
                // add counter of unread
            }
        }
        adapter.notifyItemRangeChanged(0, itemCount, "avatars")
    }

    private fun setUnreadCounterVisibility(isVisible: Boolean) {
        if (view == null) return

        binding.unreadCounter.run {
            if (isVisible) {
                show()
            } else {
                hide()
            }
        }
    }

    private class AttachmentPanelController {
        companion object {
            fun init(
                context: Context,
                adapter: MessagesHistoryAdapter,
                lifecycleOwner: LifecycleOwner,
                binding: FragmentMessagesHistoryBinding,
                isAttachmentsEmpty: () -> Boolean,
            ): AttachmentPanelController {
                val controller = AttachmentPanelController().apply {
                    this.context = context
                    this.binding = binding
                    this.adapter = adapter
                    this.isAttachmentsEmpty = isAttachmentsEmpty
                    this.message.listenValue(
                        lifecycleOwner.lifecycleScope,
                        this::onMessageValueChanged
                    )

                    this.message.update { null }
                }


                return controller
            }
        }

        val isPanelVisible = MutableStateFlow(false)
        val message = MutableStateFlow<VkMessage?>(null)

        var isEditing = false

        var adapter: MessagesHistoryAdapter by Delegates.notNull()
        var binding: FragmentMessagesHistoryBinding by Delegates.notNull()
        var context: Context by Delegates.notNull()
        var isAttachmentsEmpty: () -> Boolean by Delegates.notNull()

        fun onMessageValueChanged(value: VkMessage?) {
            if (value != null) {
                applyMessage(value)
            } else {
                clearMessage()
            }
        }

        private fun applyMessage(message: VkMessage) {
            val messageUser: VkUser? =
                if (message.isUser()) adapter.profiles[message.fromId]
                else null
            val messageGroup: VkGroup? =
                if (message.isGroup()) adapter.groups[message.fromId]
                else null
            val title = VkUtils.getMessageTitle(
                message, messageUser, messageGroup
            )

            val attachmentText = (if (message.text == null) VkUtils.getAttachmentText(
                message = message
            ) else null)?.parseString(context)

            val forwardsMessage = (if (message.text == null) VkUtils.getForwardsText(
                message = message
            ) else null)?.parseString(context)

            val messageText = forwardsMessage ?: attachmentText
            ?: (message.text ?: "").run { VkUtils.prepareMessageText(this) }

            binding.replyMessageTitle.text = title
            binding.replyMessageText.text = messageText

            if (isEditing) {
                binding.message.setText(message.text)
                binding.message.setSelection(message.text?.length ?: 0)
                binding.message.requestFocusFromTouch()
                binding.message.showKeyboard()
            }

            binding.replyMessage.visible()

            showPanel()
        }

        private fun clearMessage() {
            if (isAttachmentsEmpty()) {
                hidePanel()
            }

            binding.replyMessage.gone()

            binding.replyMessageTitle.clear()
            binding.replyMessageText.clear()

            if (isEditing) {
                isEditing = false
                binding.message.clear()
            }
        }

        fun showPanel() {
            if (isPanelVisible.value) return

            binding.attachmentPanel.visible()
//            binding.attachmentPanel.measure(
//                View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED
//            )

            if (!isPanelVisible.value)
                isPanelVisible.update { true }

//            binding.attachmentPanel.visible()

//            val measuredHeight = binding.attachmentPanel.measuredHeight
//
//            binding.attachmentPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                height = 0
//            }
//
//            binding.attachmentPanel.animate()
//                .translationY(0f)
//                .setDuration(ATTACHMENT_PANEL_ANIMATION_DURATION)
//                .start()
//
//            ValueAnimator.ofInt(0, measuredHeight).apply {
//                duration = ATTACHMENT_PANEL_ANIMATION_DURATION
//                interpolator = LinearInterpolator()
//
//                addUpdateListener { animator ->
//                    if (view == null) return@addUpdateListener
//                    val value = animator.animatedValue as Int
//
//                    if (value >= 36.dpToPx()) {
//                        binding.attachmentPanel.visible()
//                    }
//
//                    binding.attachmentPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                        height = value
//                    }
//                }
//            }.start()
        }

        fun hidePanel() {
            if (!isPanelVisible.value ||
                !isAttachmentsEmpty() ||
                message.value != null
            ) return

            if (isPanelVisible.value)
                isPanelVisible.update { false }

            binding.attachmentPanel.gone()

//            val currentHeight = binding.attachmentPanel.height
//
//            binding.attachmentPanel.animate()
//                .translationY(75F)
//                .setDuration(ATTACHMENT_PANEL_ANIMATION_DURATION)
//                .start()
//
//            ValueAnimator.ofInt(currentHeight, 0).apply {
//                duration = ATTACHMENT_PANEL_ANIMATION_DURATION
//                interpolator = LinearInterpolator()
//
//                addUpdateListener { animator ->
//                    if (view == null) return@addUpdateListener
//                    val value = animator.animatedValue as Int
//
//                    if (value <= 36.dpToPx()) {
//                        binding.attachmentPanel.gone()
//                    }
//
//                    binding.attachmentPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                        height = value
//                    }
//                }
//                doOnEnd {
//                    if (view == null) return@doOnEnd
//                    binding.attachmentPanel.gone()
//                }
//            }.start()
        }
    }

    fun openForwardsScreen(
        conversation: VkConversationDomain,
        messages: List<VkMessage>,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf(),
    ) {
        router.navigateTo(
            Screens.ForwardedMessages(conversation, messages, profiles, groups)
        )
    }

    private fun openChatInfoScreen(
        conversation: VkConversationDomain,
        user: VkUser?,
        group: VkGroup?,
    ) {
        router.navigateTo(
            Screens.ChatInfo(conversation, user, group)
        )
    }

    companion object {
        const val ARG_USER: String = "user"
        const val ARG_GROUP: String = "group"
        const val ARG_CONVERSATION: String = "conversation"

        private const val ATTACHMENT_PANEL_ANIMATION_DURATION = 150L

        fun newInstance(
            conversation: VkConversationDomain,
            user: VkUser?,
            group: VkGroup?,
        ): MessagesHistoryFragment {
            val fragment = MessagesHistoryFragment()
            fragment.arguments = bundleOf(
                ARG_CONVERSATION to conversation,
                ARG_USER to user,
                ARG_GROUP to group
            )

            return fragment
        }
    }

}
