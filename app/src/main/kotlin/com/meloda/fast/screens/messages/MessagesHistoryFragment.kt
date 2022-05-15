package com.meloda.fast.screens.messages

import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.viewbinding.library.fragment.viewBinding
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.net.MediaType
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.network.files.FilesDataSource
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.DialogMessageDeleteBinding
import com.meloda.fast.databinding.FragmentMessagesHistoryBinding
import com.meloda.fast.extensions.*
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.screens.conversations.MessagesNewEvent
import com.meloda.fast.screens.settings.SettingsPrefsFragment
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

@AndroidEntryPoint
class MessagesHistoryFragment :
    BaseViewModelFragment<MessagesHistoryViewModel>(R.layout.fragment_messages_history) {

    companion object {
        const val ARG_USER: String = "user"
        const val ARG_GROUP: String = "group"
        const val ARG_CONVERSATION: String = "conversation"

        private const val ATTACHMENT_PANEL_ANIMATION_DURATION = 150L

        fun newInstance(
            conversation: VkConversation,
            user: VkUser?,
            group: VkGroup?
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

    override val viewModel: MessagesHistoryViewModel by viewModels()
    private val binding: FragmentMessagesHistoryBinding by viewBinding()

    private var pickFile: Boolean = false

    private val attachmentsToLoad = mutableListOf<VkAttachment>()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList: List<Uri>? ->
            if (uriList.isNullOrEmpty()) {
                return@registerForActivityResult
            }

//            if (uriList.size > 1) {
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle(R.string.warning)
//                    .setMessage("At the moment you can attach only one item")
//                    .setPositiveButton(R.string.ok, null)
//                    .show()
//                return@registerForActivityResult
//            }

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

//                uriList.forEach { processFileFromStorage(it) }
            }
        }


    private val actionState = MutableLiveData<Action>()

    private enum class Action {
        RECORD, SEND, EDIT, DELETE
    }

    private val user: VkUser? by lazy {
        requireArguments().getParcelable(ARG_USER)
    }

    private val group: VkGroup? by lazy {
        requireArguments().getParcelable(ARG_GROUP)
    }

    private val conversation: VkConversation by lazy {
        requireNotNull(requireArguments().getParcelable(ARG_CONVERSATION))
    }

    private val adapter: MessagesHistoryAdapter by lazy {
        MessagesHistoryAdapter(this, conversation).also {
            it.itemClickListener = this::onItemClick
            it.avatarLongClickListener = this::onAvatarLongClickListener
        }
    }

    private var timestampTimer: Timer? = null

    private lateinit var attachmentController: AttachmentPanelController

    init {
        shouldNavBarShown = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        attachmentController = AttachmentPanelController().init()

        val title = when {
            conversation.isChat() -> conversation.title
            conversation.isUser() -> user?.toString()
            conversation.isGroup() -> group?.name
            else -> null
        }

        binding.toolbar.title = title ?: "..."

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

        binding.toolbar.subtitle = status ?: "..."

        prepareAvatar()

        prepareViews()

        binding.recyclerView.adapter = adapter

        viewModel.loadHistory(conversation.id)

        binding.action.setOnClickListener { performAction() }

        binding.recyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom >= oldBottom) return@addOnLayoutChangeListener
            val lastVisiblePosition =
                (binding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            if (lastVisiblePosition <= adapter.lastPosition - 10) return@addOnLayoutChangeListener

            binding.recyclerView.postDelayed({
                if (getView() == null) return@postDelayed
                binding.recyclerView.scrollToPosition(adapter.lastPosition)
            }, 25)
        }

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
                        SettingsPrefsFragment.PrefHideKeyboardOnScroll,
                        true
                    ) && dy < 0
                ) {
                    binding.recyclerView.hideKeyboard()
                }

                setUnreadCounterVisibility(lastPosition, dy)

                adapter.getOrNull(firstPosition)?.let {
                    if (it !is VkMessage) return
                    binding.timestamp.visible()

                    val time = "${
                        TimeUtils.getLocalizedDate(
                            requireContext(),
                            it.date * 1000L
                        )
                    }, ${
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(it.date * 1000L)
                    }"

                    binding.timestamp.text = time

                    if (timestampTimer != null) {
                        timestampTimer?.cancel()
                        timestampTimer = null
                    }

                    timestampTimer = Timer()
                    timestampTimer?.schedule(2500) {
                        recyclerView.post { binding.timestamp.gone() }
                    }
                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })

        binding.message.doAfterTextChanged {
            val canSend = it.toString().isNotBlank()

            val newValue: Action =
                when {
                    attachmentController.isEditing -> if (it.isNullOrBlank()) Action.DELETE else Action.EDIT
                    canSend -> Action.SEND
                    else -> Action.RECORD
                }

            if (actionState.value != newValue) actionState.value = newValue
        }

        actionState.observe(viewLifecycleOwner) {
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

            when (it) {
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
                else -> return@observe
            }
        }

        attachmentController.isPanelVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible) binding.message.setSelection(binding.message.text.toString().length)

            val currentHeight = binding.listAnchor.height

            val newHeight =
                if (isVisible) (binding.attachmentPanel.measuredHeight / 1.5).roundToInt()
                else 1

            ValueAnimator.ofInt(currentHeight, newHeight).apply {
                duration = ATTACHMENT_PANEL_ANIMATION_DURATION
                interpolator = LinearInterpolator()

                addUpdateListener { animator ->
                    if (getView() == null) return@addUpdateListener
                    val value = animator.animatedValue as Int

                    binding.listAnchor.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = value
                    }
                }
            }.start()
        }

        binding.attachmentPanel.setOnClickListener {
            val message = attachmentController.message.value ?: return@setOnClickListener
            val index = adapter.searchMessageIndex(message.id) ?: return@setOnClickListener

            binding.recyclerView.scrollToPosition(index)
        }

        binding.dismissReply.setOnClickListener {
            if (attachmentController.message.value != null)
                attachmentController.message.value = null
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

    override fun toggleProgress(isProgressing: Boolean) {
        view?.run {
            findViewById<View>(R.id.progress_bar).toggleVisibility(
                if (isProgressing) adapter.isEmpty() else false
            )
            findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing =
                if (isProgressing) adapter.isNotEmpty() else false
        }
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
            @Suppress("BlockingMethodInNonBlockingContext")
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
                FilesDataSource.FileType.File
            )

            uploadedAttachment?.run {
                addAttachment(this)
            }
        } else {
            when (MediaType.parse(mimeType).type()) {
                MediaType.ANY_IMAGE_TYPE.type() -> {
                    viewModel.getPhotoMessageUploadServer(conversation.id, file, name)
                }
                MediaType.ANY_VIDEO_TYPE.type() -> {
                    viewModel.getVideoMessageUploadServer(conversation.id, file, name)
                }
                MediaType.ANY_AUDIO_TYPE.type() -> {
                    viewModel.getAudioUploadServer(conversation.id, file, name)
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
    }

    private fun clearAttachments() {
        attachmentsToLoad.clear()
        binding.attachmentsCounter.gone()
        binding.attachmentsCounter.text = null
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
            conversation.isChat() -> conversation.photo200
            else -> null
        }

        val avatarMenuItem = binding.toolbar.addAvatarMenuItem()
        val avatarImageView: ImageView = avatarMenuItem.actionView.findViewById(R.id.avatar)

        avatarImageView.loadWithGlide(url = avatar, asCircle = true, crossFade = true)
    }

    private fun performAction() {
        when (actionState.value) {
            Action.RECORD -> {
            }
            Action.SEND -> {
                val messageText = binding.message.text.toString().trim()
                if (messageText.isBlank()) return

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
                    randomId = Random.nextInt(-25000, 25000),
                    replyMessage = attachmentController.message.value,
                    attachments = attachments
                )

                Log.d("LongPollUpdatesParser", "newMessageRandomId: ${message.randomId}")

                adapter.add(message, beforeFooter = true, commitCallback = {
                    binding.recyclerView.scrollToPosition(adapter.lastPosition)
                    binding.message.clear()
                })

                val replyMessage = attachmentController.message.value
                attachmentController.message.value = null

                viewModel.sendMessage(
                    peerId = conversation.id,
                    message = messageText,
                    randomId = message.randomId,
                    replyTo = replyMessage?.id,
                    setId = { messageId ->
                        val messageToUpdate = adapter[messageIndex] as VkMessage
                        messageToUpdate.id = messageId
                        adapter[messageIndex] = messageToUpdate
                    },
                    attachments = attachments
                )
            }
            Action.EDIT -> {
                val message = attachmentController.message.value ?: return
                val messageText = binding.message.text.toString().trim()

                attachmentController.message.value = null

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
            else -> {}
        }
    }


    private fun prepareViews() {
        prepareRecyclerView()
        prepareRefreshLayout()
        prepareEmojiButton()
    }

    private fun prepareRecyclerView() {
        binding.recyclerView.itemAnimator = null
    }

    private fun prepareRefreshLayout() {
        with(binding.refreshLayout) {
            setProgressViewOffset(
                true, progressViewStartOffset, progressViewEndOffset
            )
            setProgressBackgroundColorSchemeColor(
                AndroidUtils.getThemeAttrColor(
                    requireContext(),
                    R.attr.colorSurface
                )
            )
            setColorSchemeColors(
                AndroidUtils.getThemeAttrColor(
                    requireContext(),
                    R.attr.colorAccent
                )
            )
            setOnRefreshListener { viewModel.loadHistory(peerId = conversation.id) }
        }
    }

    private fun prepareEmojiButton() {
        binding.emoji.setOnLongClickListener {
            val text = binding.message.text.toString() + AppGlobal.preferences.getString(
                SettingsPrefsFragment.PrefFastText, SettingsPrefsFragment.PrefFastTextDefaultValue
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
            true
        }
    }

    private fun markMessagesAsImportant(event: MessagesMarkAsImportantEvent) {
        val newList = adapter.cloneCurrentList()

        for (i in newList.indices) {
            val item = newList[i]
            val message: VkMessage = (if (item !is VkMessage) null else item) ?: continue
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
            withHeader = true,
            withFooter = true,
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
        val message = adapter[position] as VkMessage

        val messageUser = VkUtils.getMessageUser(message, adapter.profiles)
        val messageGroup = VkUtils.getMessageGroup(message, adapter.groups)

        val title = VkUtils.getMessageTitle(message, messageUser, messageGroup)
        Toast.makeText(requireContext(), title, Toast.LENGTH_SHORT).show()
    }

    private fun showOptionsDialog(position: Int) {
        val message = adapter[position] as VkMessage
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

        val delete = getString(R.string.message_context_action_delete)

        val params = mutableListOf(reply)

        if (conversation.canChangePin) {
            params += pin
        }

        params += important

        if (!message.isRead(conversation) && !message.isOut) {
            params += read
        }

        if (message.canEdit()) {
            params += edit
        }

        params += delete

        val arrayParams = params.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(time)
            .setItems(arrayParams) { _, which ->
                when (params[which]) {
                    reply -> {
                        if (attachmentController.message.value != message)
                            attachmentController.message.value = message
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
                            attachmentController.message.value = message
                    }
                    delete -> showDeleteMessageDialog(message)
                }
            }.show()
    }

    private fun showPinMessageDialog(
        peerId: Int,
        messageId: Int?,
        pin: Boolean
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
            if (message.isOut) R.string.message_delete_for_all
            else R.string.message_mark_as_spam
        )

        binding.check.isEnabled =
            (conversation.id != UserConfig.userId) && (!message.isOut || message.canEdit())

        if (conversation.id == UserConfig.userId ||
            (binding.check.isEnabled && message.isOut)
        ) binding.check.isChecked = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete_message)
            .setView(binding.root)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                attachmentController.message.value = null

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

        if (event.isOut) {
            conversation.outRead = event.messageId
        } else {
            conversation.inRead = event.messageId
        }

        val positionsToUpdate = mutableListOf<Int>()
        val newList = adapter.cloneCurrentList()
        for (i in newList.indices) {
            val message = newList[i]
            if (message !is VkMessage) continue

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

    @Suppress("NAME_SHADOWING")
    private fun setUnreadCounterVisibility(
        lastCompletelyVisiblePosition: Int,
        dy: Int? = null
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

        adapter.add(event.message, beforeFooter = true) {
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

    private inner class AttachmentPanelController {
        val isPanelVisible = MutableLiveData(false)
        val message = MutableLiveData<VkMessage?>()

        var isEditing = false

        fun init(): AttachmentPanelController {
            message.observe(viewLifecycleOwner) { value ->
                if (value != null) {
                    applyMessage(value)
                } else {
                    clearMessage()
                }
            }

            message.value = null
            return this
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

            val attachmentText = if (message.text == null) VkUtils.getAttachmentText(
                context = requireContext(),
                message = message
            ) else null

            val forwardsMessage = if (message.text == null) VkUtils.getForwardsText(
                context = requireContext(),
                message = message
            ) else null

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

            showPanel()
        }

        private fun clearMessage() {
            hidePanel()

            binding.replyMessageTitle.clear()
            binding.replyMessageText.clear()

            if (isEditing) {
                isEditing = false
                binding.message.clear()
            }
        }

        private fun showPanel() {
            binding.attachmentPanel.visible()
            binding.attachmentPanel.measure(
                View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED
            )

            if (attachmentController.isPanelVisible.value == false)
                attachmentController.isPanelVisible.value = true

            val measuredHeight = binding.attachmentPanel.measuredHeight

            binding.attachmentPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = 0
            }

            binding.attachmentPanel.animate()
                .translationY(0f)
                .setDuration(ATTACHMENT_PANEL_ANIMATION_DURATION)
                .start()

            ValueAnimator.ofInt(0, measuredHeight).apply {
                duration = ATTACHMENT_PANEL_ANIMATION_DURATION
                interpolator = LinearInterpolator()

                addUpdateListener { animator ->
                    if (view == null) return@addUpdateListener
                    val value = animator.animatedValue as Int

                    if (value >= 36.dpToPx()) {
                        binding.attachmentPanel.visible()
                    }

                    binding.attachmentPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = value
                    }
                }
            }.start()
        }

        private fun hidePanel() {
            if (attachmentController.isPanelVisible.value == true)
                attachmentController.isPanelVisible.value = false

            val currentHeight = binding.attachmentPanel.height

            binding.attachmentPanel.animate()
                .translationY(75F)
                .setDuration(ATTACHMENT_PANEL_ANIMATION_DURATION)
                .start()

            ValueAnimator.ofInt(currentHeight, 0).apply {
                duration = ATTACHMENT_PANEL_ANIMATION_DURATION
                interpolator = LinearInterpolator()

                addUpdateListener { animator ->
                    if (view == null) return@addUpdateListener
                    val value = animator.animatedValue as Int

                    if (value <= 36.dpToPx()) {
                        binding.attachmentPanel.gone()
                    }

                    binding.attachmentPanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = value
                    }
                }
                doOnEnd {
                    if (view == null) return@doOnEnd
                    binding.attachmentPanel.gone()
                }
            }.start()
        }
    }

    fun openForwardsScreen(
        conversation: VkConversation,
        messages: List<VkMessage>,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf()
    ) {
        requireActivityRouter().navigateTo(
            Screens.ForwardedMessages(conversation, messages, profiles, groups)
        )
    }

}