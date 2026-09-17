package dev.meloda.fast.messageshistory

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import coil.imageLoader
import coil.request.ImageRequest
import com.conena.nanokt.collections.indexOfFirstOrNull
import com.slack.eithernet.ApiResult
import dev.meloda.fast.common.ActiveChatTracker
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.getParcelableCompat
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.common.extensions.removeIfCompat
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.data.State
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.api.files.FilesRepository
import dev.meloda.fast.data.api.photos.PhotosRepository
import dev.meloda.fast.data.api.stickers.StickersRepository
import dev.meloda.fast.data.api.videos.VideosRepository
import dev.meloda.fast.data.processState
import dev.meloda.fast.data.success
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.ConvoUseCase
import dev.meloda.fast.domain.GetMessageReadPeersUseCase
import dev.meloda.fast.domain.LoadConvosByIdUseCase
import dev.meloda.fast.domain.LongPollEventsHandler
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.domain.util.asPresentation
import dev.meloda.fast.domain.util.extractAvatar
import dev.meloda.fast.domain.util.extractReplySummary
import dev.meloda.fast.domain.util.extractReplyTitle
import dev.meloda.fast.domain.util.extractTitle
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.messageshistory.model.MessageDialog
import dev.meloda.fast.messageshistory.model.MessageNavigation
import dev.meloda.fast.messageshistory.model.MessageOption
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.VoicePlaybackState
import dev.meloda.fast.messageshistory.navigation.MessagesHistory
import dev.meloda.fast.messageshistory.player.VoiceMessagesPlayer
import dev.meloda.fast.messageshistory.recorder.VoiceRecorder
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.PhotoSize
import dev.meloda.fast.model.api.domain.FormatDataType
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAudioMessageDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkPhotoDomain
import dev.meloda.fast.model.api.domain.VkStickerDomain
import dev.meloda.fast.model.api.domain.VkStickerPackDomain
import dev.meloda.fast.model.api.domain.VkVideoDomain
import dev.meloda.fast.model.api.domain.VkVideoMessageDomain
import dev.meloda.fast.model.api.requests.PhotosSaveMessagePhotoRequest
import dev.meloda.fast.network.VkErrorCode
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.vk.MessageUiItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import dev.meloda.fast.logger.FastLogger
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.random.Random

class MessagesHistoryViewModelImpl(
    private val applicationContext: Context,
    private val messagesUseCase: MessagesUseCase,
    private val filesRepository: FilesRepository,
    private val videosRepository: VideosRepository,
    private val stickersRepository: StickersRepository,
    private val photosRepository: PhotosRepository,
    private val convoUseCase: ConvoUseCase,
    private val resourceProvider: ResourceProvider,
    private val userSettings: UserSettings,
    private val loadConvosByIdUseCase: LoadConvosByIdUseCase,
    private val getMessageReadPeersUseCase: GetMessageReadPeersUseCase,
    private val logger: FastLogger,
    eventsHandler: LongPollEventsHandler,
    savedStateHandle: SavedStateHandle
) : MessagesHistoryViewModel, ViewModel() {

    override val screenState = MutableStateFlow(MessagesHistoryScreenState.EMPTY)
    override val navigation = MutableStateFlow<MessageNavigation?>(null)
    override val dialog = MutableStateFlow<MessageDialog?>(null)
    override val selectedMessages = MutableStateFlow<List<VkMessage>>(emptyList())

    override val showKeyboard = MutableStateFlow(false)

    override val isNeedToScrollToIndex = MutableStateFlow<Int?>(null)

    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())

    override val currentOffset = MutableStateFlow(0)

    override val canPaginate = MutableStateFlow(false)

    override val messages = MutableStateFlow<List<VkMessage>>(emptyList())
    override val uiMessages = MutableStateFlow<List<MessageUiItem>>(emptyList())

    override val voicePlayback: StateFlow<VoicePlaybackState> get() = voicePlayer.state

    override val isRecordingVoice: StateFlow<Boolean> get() = voiceRecorder.isRecording
    override val voiceRecordingDurationSec: StateFlow<Int> get() = voiceRecorder.durationSec

    private val _isRecordingVideoMessage = MutableStateFlow(false)
    override val isRecordingVideoMessage: StateFlow<Boolean> = _isRecordingVideoMessage.asStateFlow()

    private val _stickerPacks = MutableStateFlow<List<VkStickerPackDomain>>(emptyList())
    override val stickerPacks: StateFlow<List<VkStickerPackDomain>> = _stickerPacks.asStateFlow()

    private val _isStickerPickerOpen = MutableStateFlow(false)
    override val isStickerPickerOpen: StateFlow<Boolean> = _isStickerPickerOpen.asStateFlow()

    private var stickerPacksLoaded = false

    private val _isAttachmentPickerOpen = MutableStateFlow(false)
    override val isAttachmentPickerOpen: StateFlow<Boolean> = _isAttachmentPickerOpen.asStateFlow()

    private val _pickPhotoRequest = MutableStateFlow(0)
    override val pickPhotoRequest: StateFlow<Int> = _pickPhotoRequest.asStateFlow()

    private var lastMessageText: String? = null

    private val sendingMessages: MutableList<VkMessage> = mutableListOf()
    private val failedMessages: MutableList<VkMessage> = mutableListOf()

    private var replyToCmId: Long? = null

    private var editMessage: VkMessage? = null

    private val voicePlayer = VoiceMessagesPlayer(applicationContext, viewModelScope)
    private val voiceRecorder = VoiceRecorder(applicationContext, viewModelScope)

    init {
        val arguments = MessagesHistory.from(savedStateHandle).arguments

        screenState.setValue { old -> old.copy(convoId = arguments.convoId) }

        ActiveChatTracker.onChatOpened(arguments.convoId)

        loadConvo()
        loadMessagesHistory()

        eventsHandler.onMessageNew(::handleNewMessage)
        eventsHandler.onMessageEdit(::handleEditedMessage)
        eventsHandler.onMessageIncomingRead(::handleReadIncomingEvent)
        eventsHandler.onMessageOutgoingRead(::handleReadOutgoingEvent)
        eventsHandler.onMessageDelete(::handleMessageDeleted)
        eventsHandler.onMessageRestore(::handleMessageRestored)
        eventsHandler.onMessageMarkAsImportant(::handleMessageMarkedAsImportant)
        eventsHandler.onMessageMarkAsSpam(::handleMessageMarkedAsSpam)
        eventsHandler.onMessageMarkAsNotSpam(::handleMessageMarkedAsNotSpam)
    }

    override fun onNavigationConsumed() {
        navigation.setValue { null }
    }

    override fun onTopBarClicked() {
        val convoId = screenState.value.convoId
        if (convoId in 1 until 2_000_000_000L) {
            navigation.setValue {
                MessageNavigation.Profile(userId = convoId)
            }
        } else {
            val cmId = messages.value.firstOrNull()?.cmId ?: 0L
            navigation.setValue {
                MessageNavigation.ChatMaterials(
                    peerId = convoId,
                    cmId = cmId
                )
            }
        }
    }

    override fun onDialogConfirmed(dialog: MessageDialog, bundle: Bundle) {
        onDialogDismissed(dialog)

        when (dialog) {
            is MessageDialog.MessageOptions -> Unit

            is MessageDialog.MessageDelete -> {
                val deleteForEveryone = bundle.getBoolean("everyone")

                if (dialog.message.id <= 0) {
                    val newMessages = messages.value.toMutableList()
                    newMessages.remove(dialog.message)
                    messages.setValue { newMessages }
                    syncUiMessages()
                    return
                }

                deleteMessage(
                    messageIds = listOf(dialog.message.id),
                    deleteForAll = deleteForEveryone
                )
            }

            is MessageDialog.MessagesDelete -> {
                val deleteForEveryone = bundle.getBoolean("everyone")

                val failedMessages = dialog.messages.filter { it.id <= 0 }
                val messageIdsToDelete =
                    dialog.messages
                        .filter { it.id > 0 }
                        .map(VkMessage::id)

                deleteMessage(
                    messageIds = messageIdsToDelete,
                    deleteForAll = deleteForEveryone,
                    onSuccess = {
                        val newMessages = messages.value.toMutableList()
                        newMessages.removeAll(failedMessages)
                        messages.setValue { newMessages }
                        selectedMessages.setValue { emptyList() }
                        syncUiMessages()
                    }
                )
            }

            is MessageDialog.MessagePin -> {
                pinMessage(dialog.messageId)
            }

            is MessageDialog.MessageUnpin -> {
                unpinMessage(dialog.messageId)
            }

            is MessageDialog.MessageMarkImportance -> {
                markAsImportant(
                    messageIds = listOf(dialog.message.id),
                    important = dialog.isImportant
                )
            }

            is MessageDialog.MessageSpam -> {
                if (dialog.isSpam) {
                    deleteMessage(
                        messageIds = listOf(dialog.message.id),
                        spam = true
                    )
                } else {
                    // TODO: 29-Mar-25, Danil Nikolaev: report as not spam
                }
            }
        }
    }

    override fun onDialogDismissed(dialog: MessageDialog) {
        this.dialog.setValue { null }
    }

    override fun onDialogItemPicked(dialog: MessageDialog, bundle: Bundle) {
        when (dialog) {
            is MessageDialog.MessageOptions -> {
//                val messageId = bundle.getLong("messageId")
                val cmId = bundle.getLong("cmId")

                when (val option = bundle.getParcelableCompat("option", MessageOption::class)) {
                    null -> Unit

                    MessageOption.Retry -> {
                        // TODO: 28-Mar-25, Danil Nikolaev: retry sending
                    }

                    MessageOption.Reply -> replyToMessage(cmId)

                    MessageOption.ForwardHere -> {

                    }

                    MessageOption.Forward -> {

                    }

                    MessageOption.Pin -> {
                        this.dialog.setValue {
                            MessageDialog.MessagePin(dialog.message.id)
                        }
                    }

                    MessageOption.Unpin -> {
                        this.dialog.setValue {
                            MessageDialog.MessageUnpin(dialog.message.id)
                        }
                    }

                    MessageOption.Read -> {
                        readMessage(dialog.message)
                    }

                    MessageOption.Copy -> {
                        copyMessage(dialog.message)
                    }

                    MessageOption.MarkAsImportant,
                    MessageOption.UnmarkAsImportant -> {
                        this.dialog.setValue {
                            MessageDialog.MessageMarkImportance(
                                message = dialog.message,
                                isImportant = option is MessageOption.MarkAsImportant
                            )
                        }
                    }

                    MessageOption.MarkAsSpam,
                    MessageOption.UnmarkAsSpam -> {
                        this.dialog.setValue {
                            MessageDialog.MessageSpam(
                                message = dialog.message,
                                isSpam = option is MessageOption.MarkAsSpam
                            )
                        }
                    }

                    MessageOption.Edit -> {
                        editMessage(cmId)
                        syncUiMessages()
                    }

                    MessageOption.Delete -> {
                        this.dialog.setValue {
                            MessageDialog.MessageDelete(dialog.message)
                        }
                    }
                }
            }

            is MessageDialog.MessageDelete -> Unit
            is MessageDialog.MessageUnpin -> Unit
            is MessageDialog.MessageMarkImportance -> Unit
            is MessageDialog.MessageSpam -> Unit
            is MessageDialog.MessagePin -> Unit
            is MessageDialog.MessagesDelete -> Unit
        }
    }

    override fun onScrolledToIndex() {
        isNeedToScrollToIndex.setValue { null }
    }

    override fun onCloseButtonClicked() {
        if (selectedMessages.value.isNotEmpty()) {
            selectedMessages.setValue { emptyList() }
        }

        if (screenState.value.editCmId != null) {
            stopEditMessage()
        }

        syncUiMessages()
    }

    override fun onRefresh() {
        loadMessagesHistory(offset = 0)
    }

    override fun onAttachmentButtonClicked() {
        _isAttachmentPickerOpen.value = true
    }

    override fun onAttachmentPickerDismissed() {
        _isAttachmentPickerOpen.value = false
    }

    override fun onPickPhotoClicked() {
        _isAttachmentPickerOpen.value = false
        _pickPhotoRequest.update { it + 1 }
    }

    override fun onEmojiSelected(emoji: String) {
        val current = screenState.value.message
        val start = current.selection.min
        val end = current.selection.max
        val newText = current.text.replaceRange(start, end, emoji)
        onMessageInputChanged(
            TextFieldValue(text = newText, selection = TextRange(start + emoji.length))
        )
    }

    override fun onMessageInputChanged(newText: TextFieldValue) {
        if (voiceRecorder.isRecording.value) return

        screenState.setValue { old ->
            old.copy(
                message = newText,
                actionMode =
                    when {
                        voiceRecorder.isRecording.value -> ActionMode.RECORD_AUDIO
                        screenState.value.editCmId != null -> {
                            // TODO: 13/03/2026, Danil Nikolaev: also check if attachments is empty
                            if (newText.text.trim().isEmpty()) {
                                ActionMode.DELETE
                            } else {
                                ActionMode.EDIT
                            }
                        }

                        newText.text.trim().isEmpty() -> ActionMode.RECORD_AUDIO
                        else -> ActionMode.SEND
                    }
            )
        }
        updateStyles()
    }

    override fun onEmojiButtonLongClicked() {
        AppSettings.Features.fastText.takeIf { it.isNotBlank() }?.let { text ->
            val newText = "${screenState.value.message.text}$text"
            onMessageInputChanged(
                TextFieldValue(text = newText, selection = TextRange(newText.length))
            )
        }
    }

    override fun onActionButtonClicked() {
        when (screenState.value.actionMode) {
            ActionMode.DELETE -> confirmDeleteCurrentEditMessage()

            ActionMode.EDIT -> editCurrentEditMessage()

            ActionMode.RECORD_AUDIO -> {
                if (voiceRecorder.isRecording.value) {
                    sendVoiceRecording()
                } else {
                    screenState.setValue { it.copy(actionMode = ActionMode.RECORD_VIDEO) }
                }
            }

            ActionMode.RECORDING -> sendVoiceRecording()

            ActionMode.RECORD_VIDEO -> {
                screenState.setValue { it.copy(actionMode = ActionMode.RECORD_AUDIO) }
            }

            ActionMode.SEND -> sendMessage()
        }
    }

    override fun onRecordStart() {
        when (screenState.value.actionMode) {
            ActionMode.RECORD_AUDIO -> {
                if (!voiceRecorder.isRecording.value) {
                    startVoiceRecording()
                }
            }

            ActionMode.RECORD_VIDEO -> {
                onStartVideoMessageRecord()
            }

            else -> Unit
        }
    }

    override fun onRecordFinish() {
        if (voiceRecorder.isRecording.value) {
            sendVoiceRecording()
        }
    }

    override fun onRecordCancel() {
        if (voiceRecorder.isRecording.value) {
            cancelVoiceRecording()
        }
    }

    override fun onStartVideoMessageRecord() {
        _isRecordingVideoMessage.value = true
    }

    override fun onCancelVideoMessageRecord() {
        _isRecordingVideoMessage.value = false
    }

    override fun onEmojiButtonClicked() {
        _isStickerPickerOpen.value = true
        if (!stickerPacksLoaded) {
            loadStickerPacks()
        }
    }

    override fun onStickerPickerDismissed() {
        _isStickerPickerOpen.value = false
    }

    private fun loadStickerPacks() {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                runCatching {
                    stickersRepository.getProducts().mapApiDefault()
                }.getOrNull()
                    ?.let { result -> (result as? ApiResult.Success)?.value }
                    ?.items
                    .orEmpty()
                    .map { it.toDomain() }
                    .filter { it.stickerIds.isNotEmpty() }
            }

            _stickerPacks.value = items
            stickerPacksLoaded = items.isNotEmpty()
        }
    }

    override fun onSendSticker(stickerId: Long) {
        _isStickerPickerOpen.value = false

        val stickerAttachment = VkStickerDomain(
            id = stickerId,
            productId = 0,
            images = null,
            backgroundImages = null
        )

        sendMessage(
            attachments = listOf(stickerAttachment),
            stickerId = stickerId
        )
    }

    override fun onSendPhoto(uri: Uri) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { copyUriToCache(uri) }
            if (file == null) {
                Toast.makeText(applicationContext, "Не удалось прочитать фото", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val placeholderPhoto = VkPhotoDomain(
                albumId = 0,
                date = null,
                id = -1L - sendingMessages.size,
                ownerId = UserConfig.userId,
                hasTags = false,
                accessKey = null,
                sizes = listOf(
                    PhotoSize(
                        height = 604,
                        width = 604,
                        type = VkPhotoDomain.SIZE_TYPE_604.toString(),
                        url = file.absolutePath
                    )
                ),
                text = null,
                userId = UserConfig.userId
            )

            val newMessage = VkMessage(
                id = -1L - sendingMessages.size,
                cmId = -1L - sendingMessages.size,
                text = "",
                isOut = true,
                peerId = screenState.value.convoId,
                fromId = UserConfig.userId,
                date = (System.currentTimeMillis() / 1000).toInt(),
                randomId = Random.nextInt().toLong(),
                action = null,
                actionMemberId = null,
                actionText = null,
                actionCmId = null,
                actionMessage = null,
                updateTime = null,
                isImportant = false,
                forwards = null,
                attachments = listOf(placeholderPhoto),
                replyMessage = when {
                    replyToCmId != null -> messages.value.find { it.cmId == replyToCmId }
                    else -> null
                },
                geoType = null,
                user = VkMemoryCache.getUser(UserConfig.userId),
                group = null,
                actionUser = null,
                actionGroup = null,
                isPinned = false,
                isSpam = false,
                pinnedAt = null,
                formatData = null,
                isDeleted = false
            )

            sendingMessages += newMessage
            messages.setValue { old -> listOf(newMessage).plus(old) }
            syncUiMessages()

            val replyCmId = replyToCmId
            replyToCmId = null

            screenState.setValue { old ->
                old.copy(replyTitle = null, replyText = null)
            }

            val forward = when {
                replyCmId != null -> {
                    buildJsonObject {
                        put("peer_id", screenState.value.convoId)
                        put("conversation_message_ids", buildJsonArray { add(replyCmId) })
                        put("is_reply", true)
                    }.toString()
                }

                else -> null
            }

            val uploadedPhoto = withContext(Dispatchers.IO) {
                runCatching {
                    uploadPhotoMessage(file)
                }.onFailure { error ->
                    logger.error(this@MessagesHistoryViewModelImpl::class, "uploadPhotoMessage failed", error)
                }.getOrNull()
            }

            file.delete()

            if (uploadedPhoto == null) {
                sendingMessages.remove(newMessage)
                markMessageAsFailed(newMessage)
                return@launch
            }

            val messageWithRealAttachment = newMessage.copy(attachments = listOf(uploadedPhoto))
            messages.setValue { list ->
                list.toMutableList().also { mutable ->
                    val index = mutable.indexOf(newMessage)
                    if (index != -1) {
                        mutable[index] = messageWithRealAttachment
                    }
                }
            }
            syncUiMessages()

            messagesUseCase.sendMessage(
                peerId = screenState.value.convoId,
                randomId = newMessage.randomId,
                message = "",
                forward = forward,
                attachments = listOf(uploadedPhoto),
                formatData = null,
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    any = { sendingMessages.remove(messageWithRealAttachment) },
                    error = { _ ->
                        markMessageAsFailed(messageWithRealAttachment)
                    },
                    success = { response ->
                        updateSentMessage(
                            messageWithRealAttachment,
                            response.messageId,
                            response.cmId
                        )
                    }
                )
            }
        }
    }

    private suspend fun uploadPhotoMessage(file: File): VkPhotoDomain {
        val uploadServerResponse = photosRepository
            .getMessagesUploadServer(peerId = screenState.value.convoId)
            .mapApiDefault()
            .success()

        val mimeType = applicationContext.contentResolver.getType(file.toUri()) ?: "image/jpeg"
        val requestBody = file.asRequestBody(mimeType.toMediaType())
        val body = MultipartBody.Part.createFormData("photo", file.name, requestBody)

        val uploadResponse = photosRepository
            .uploadPhoto(url = uploadServerResponse.uploadUrl, photo = body)
            .success()

        val saveResponse = photosRepository
            .saveMessagePhoto(
                PhotosSaveMessagePhotoRequest(
                    photo = uploadResponse.photo,
                    server = uploadResponse.server,
                    hash = uploadResponse.hash
                )
            )
            .mapApiDefault()
            .success()

        return saveResponse.first().toDomain()
    }

    private fun copyUriToCache(uri: Uri): File? = runCatching {
        val inputStream = applicationContext.contentResolver.openInputStream(uri)
            ?: return@runCatching null
        val file = File(applicationContext.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output -> inputStream.copyTo(output) }
        inputStream.close()
        file
    }.getOrNull()

    override fun onSendVideoMessage(file: File, durationSec: Int) {
        _isRecordingVideoMessage.value = false

        val placeholderAttachment = VkVideoMessageDomain(
            id = -1L,
            ownerId = UserConfig.userId,
            accessKey = null,
            duration = durationSec,
            image = null,
            link = file.absolutePath
        )

        val newMessage = VkMessage(
            id = -1L - sendingMessages.size,
            cmId = -1L - sendingMessages.size,
            text = "",
            isOut = true,
            peerId = screenState.value.convoId,
            fromId = UserConfig.userId,
            date = (System.currentTimeMillis() / 1000).toInt(),
            randomId = Random.nextInt().toLong(),
            action = null,
            actionMemberId = null,
            actionText = null,
            actionCmId = null,
            actionMessage = null,
            updateTime = null,
            isImportant = false,
            forwards = null,
            attachments = listOf(placeholderAttachment),
            replyMessage = when {
                replyToCmId != null -> messages.value.find { it.cmId == replyToCmId }
                else -> null
            },
            geoType = null,
            user = VkMemoryCache.getUser(UserConfig.userId),
            group = null,
            actionUser = null,
            actionGroup = null,
            isPinned = false,
            isSpam = false,
            pinnedAt = null,
            formatData = null,
            isDeleted = false
        )

        sendingMessages += newMessage
        messages.setValue { old -> listOf(newMessage).plus(old) }
        syncUiMessages()

        val replyCmId = replyToCmId
        replyToCmId = null

        screenState.setValue { old ->
            old.copy(replyTitle = null, replyText = null)
        }

        val forward = when {
            replyCmId != null -> {
                buildJsonObject {
                    put("peer_id", screenState.value.convoId)
                    put("conversation_message_ids", buildJsonArray { add(replyCmId) })
                    put("is_reply", true)
                }.toString()
            }
            else -> null
        }

        viewModelScope.launch {
            val squareFile = cropVideoToSquare(file)
            if (squareFile != file) {
                file.delete()
                val localAttachment = placeholderAttachment.copy(link = squareFile.absolutePath)
                messages.setValue { list ->
                    list.map { message ->
                        if (message.id == newMessage.id) {
                            message.copy(attachments = listOf(localAttachment))
                        } else {
                            message
                        }
                    }
                }
                syncUiMessages()
            }

            val uploadedAttachment = withContext(Dispatchers.IO) {
                runCatching {
                    uploadVideoMessage(file = squareFile, durationSec = durationSec)
                }.onFailure { error ->
                    logger.error(this@MessagesHistoryViewModelImpl::class, "uploadVideoMessage failed", error)
                }.getOrNull()
            }

            val attachment = uploadedAttachment
            if (attachment == null) {
                squareFile.delete()
                sendingMessages.remove(newMessage)
                markMessageAsFailed(newMessage)
                return@launch
            }

            messagesUseCase.sendMessage(
                peerId = screenState.value.convoId,
                randomId = newMessage.randomId,
                message = "",
                forward = forward,
                attachments = listOf(attachment),
                formatData = null,
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    any = { sendingMessages.remove(newMessage) },
                    error = { _ ->
                        squareFile.delete()
                        markMessageAsFailed(newMessage)
                    },
                    success = { response ->
                        updateSentMessage(
                            newMessage,
                            response.messageId,
                            response.cmId
                        )
                        pollVideoMessageAttachment(
                            messageId = response.messageId,
                            cmId = response.cmId,
                            localFile = squareFile
                        )
                    }
                )
            }
        }
    }

    private suspend fun uploadVideoMessage(
        file: File,
        durationSec: Int
    ): VkAttachment {
        logger.debug(this::class, "uploadVideoMessage: file=${file.path}, size=${file.length()}")
        val saveResponse = videosRepository.save(
            isVideoMessage = true,
            name = "Видеосообщение"
        ).mapApiDefault().success()

        val requestBody = file.asRequestBody("video/mp4".toMediaType())
        val body = MultipartBody.Part.createFormData("video_file", file.name, requestBody)

        videosRepository.upload(url = saveResponse.uploadUrl, file = body)

        return VkVideoDomain(
            id = saveResponse.videoid,
            ownerId = saveResponse.ownerid,
            accessKey = saveResponse.accessKey,
            images = emptyList(),
            firstFrames = null,
            title = "Видеосообщение",
            views = 0,
            duration = durationSec,
            isShortVideo = false
        )
    }

    private suspend fun cropVideoToSquare(input: File): File {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(input.absolutePath)
            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0
            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0
            val rotation = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull() ?: 0
            retriever.release()

            val frameWidth = if (rotation % 180 != 0) height else width
            val frameHeight = if (rotation % 180 != 0) width else height

            if (frameWidth <= 0 || frameHeight <= 0 || frameWidth == frameHeight) {
                return input
            }

            val output = File(input.parentFile, "square_${input.name}")

            suspendCancellableCoroutine { continuation ->
                val transformer = Transformer.Builder(applicationContext)
                    .addListener(object : Transformer.Listener {
                        override fun onCompleted(
                            composition: Composition,
                            exportResult: ExportResult
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(output)
                            }
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            logger.error(
                                this@MessagesHistoryViewModelImpl::class,
                                "cropVideoToSquare failed",
                                exportException
                            )
                            output.delete()
                            if (continuation.isActive) {
                                continuation.resume(input)
                            }
                        }
                    })
                    .build()

                val effects = Effects(
                    emptyList(),
                    listOf(
                        Presentation.createForAspectRatio(
                            1f,
                            Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
                        )
                    )
                )
                val editedMediaItem = EditedMediaItem
                    .Builder(MediaItem.fromUri(input.absolutePath))
                    .setEffects(effects)
                    .build()

                transformer.start(editedMediaItem, output.absolutePath)

                continuation.invokeOnCancellation {
                    runCatching { transformer.cancel() }
                    output.delete()
                }
            }
        } catch (error: Exception) {
            logger.error(this::class, "cropVideoToSquare failed", error)
            input
        }
    }

    private fun pollVideoMessageAttachment(
        messageId: Long,
        cmId: Long,
        localFile: File?
    ) {
        viewModelScope.launch {
            for (attempt in 0 until 30) {
                delay(2000L)

                val serverMessage = runCatching {
                    val state = messagesUseCase.getById(
                        peerCmIds = null,
                        peerId = null,
                        messageIds = listOf(messageId),
                        cmIds = null,
                        extended = null,
                        fields = null
                    ).first()
                    (state as? State.Success)?.data?.firstOrNull()
                }.getOrNull() ?: continue

                val attachment = serverMessage.attachments?.firstOrNull()

                val videoMessageAttachment = when (attachment) {
                    is VkVideoMessageDomain -> {
                        if (attachment.link.isNullOrBlank()) {
                            continue
                        }
                        attachment
                    }

                    is VkVideoDomain -> {
                        val link = attachment.directUrl
                        if (link.isNullOrBlank()) {
                            continue
                        }
                        VkVideoMessageDomain(
                            id = attachment.id,
                            ownerId = attachment.ownerId,
                            accessKey = attachment.accessKey,
                            duration = attachment.duration,
                            image = attachment.getDefault()?.url,
                            link = link
                        )
                    }

                    else -> continue
                }

                messages.setValue { list ->
                    list.map { message ->
                        if (message.id == messageId && message.cmId == cmId) {
                            message.copy(attachments = listOf(videoMessageAttachment))
                        } else {
                            message
                        }
                    }
                }
                syncUiMessages()
                localFile?.delete()
                return@launch
            }
        }
    }

    override fun onPaginationConditionsMet() {
        currentOffset.update { messages.value.size }
        loadMessagesHistory()
    }

    override fun onMessageClicked(messageId: Long) {
        val currentMessage = messages.value.firstOrNull { it.id == messageId } ?: return

        if (selectedMessages.value.isNotEmpty()) {
            val isSelected = selectedMessages.value.contains(currentMessage)

            selectedMessages.setValue { old ->
                old.toMutableList().also {
                    if (isSelected) {
                        it.remove(currentMessage)
                    } else {
                        it.add(currentMessage)
                    }
                }
            }
            syncUiMessages()
        } else {
            dialog.setValue {
                MessageDialog.MessageOptions(currentMessage)
            }
        }
    }

    override fun onMessageLongClicked(messageId: Long) {
        val currentMessage = messages.value.firstOrNull { it.id == messageId } ?: return

        val isSelected = selectedMessages.value.contains(currentMessage)
        if (isSelected) return

        selectedMessages.setValue { old ->
            old.toMutableList().also {
                it.add(currentMessage)
            }
        }
        syncUiMessages()
    }

    override fun onPinnedMessageClicked(messageId: Long) {
        val uiMessages = uiMessages.value
        val messageIndex = uiMessages.indexOfFirstOrNull {
            it is MessageUiItem.Message && it.id == messageId
        }

        if (messageIndex == null) { // сообщения нет в списке
            // pizdets
        } else {
            isNeedToScrollToIndex.setValue { messageIndex }
        }
    }

    override fun onUnpinMessageClicked() {
        val pinnedMessageId = screenState.value.pinnedMessage?.id ?: return
        dialog.setValue {
            MessageDialog.MessageUnpin(pinnedMessageId)
        }
    }

    override fun onEditSelectedMessageClicked() {
        val cmId = selectedMessages.value.firstOrNull()?.cmId ?: return

        selectedMessages.setValue { emptyList() }

        editMessage(cmId)

        syncUiMessages()
    }

    override fun onDeleteSelectedMessagesClicked() {
        dialog.setValue {
            MessageDialog.MessagesDelete(selectedMessages.value)
        }
    }

    private fun replyToMessage(cmId: Long) {
        val messageToReply = messages.value.find { it.cmId == cmId } ?: return

        showKeyboard.setValue { true }
        replyToCmId = cmId
        screenState.setValue { old ->
            old.copy(
                replyTitle = messageToReply.extractTitle(),
                replyText = messageToReply.extractReplySummary(resourceProvider.resources)
            )
        }
    }

    private fun editMessage(cmId: Long) {
        this.screenState.setValue { old ->
            old.copy(editCmId = cmId)
        }

        val messageToEdit = messages.value.firstOrNull { it.cmId == cmId } ?: return
        editMessage = messageToEdit

        lastMessageText = screenState.value.message.text

        var newState = screenState.value.copy(
            message = TextFieldValue(
                text = messageToEdit.text.orEmpty(),
                selection = TextRange(messageToEdit.text.orEmpty().length)
            ),
            actionMode = ActionMode.EDIT
        )

        messageToEdit.replyMessage?.let { reply ->
            replyToCmId = reply.cmId
            newState = newState.copy(
                replyTitle = reply.extractReplyTitle(),
                replyText = reply.extractReplySummary(resourceProvider.resources)
            )
        }

        showKeyboard.setValue { true }
        screenState.setValue { newState }
    }

    private fun stopEditMessage() {
        val lastText = lastMessageText.orEmpty().trim()

        screenState.setValue { old ->
            old.copy(
                editCmId = null,
                message = TextFieldValue(
                    text = lastText,
                    selection = TextRange(lastText.length)
                ),
                actionMode = if (lastText.isBlank()) ActionMode.RECORD_AUDIO
                else ActionMode.SEND,

                // TODO: 13/03/2026, Danil Nikolaev: use last reply
                replyTitle = null,
                replyText = null
            )
        }
    }

    private var formatData = VkMessage.FormatData("1", emptyList())

    private fun updateStyles() {
        val annotations =
            mutableListOf<AnnotatedString.Range<out AnnotatedString.Annotation>>()

        formatData.items.forEachIndexed { index, item ->
            val spanStyle = when (item.type) {
                FormatDataType.BOLD -> {
                    SpanStyle(fontWeight = FontWeight.SemiBold)
                }

                FormatDataType.ITALIC -> {
                    SpanStyle(fontStyle = FontStyle.Italic)
                }

                FormatDataType.UNDERLINE -> {
                    SpanStyle(textDecoration = TextDecoration.Underline)
                }

                FormatDataType.URL -> null
            }

            spanStyle?.let {
                annotations += AnnotatedString.Range(
                    item = spanStyle,
                    start = item.offset,
                    end = item.offset + item.length
                )
            }
        }

        val newText = AnnotatedString(
            text = screenState.value.message.text,
            annotations = annotations
        )

        screenState.setValue { old ->
            old.copy(message = old.message.copy(annotatedString = newText))
        }
    }

    override fun onBoldClicked() {
        val selectionRange = screenState.value.message.selection
        val newItems = formatData.items.toMutableList()
        val wasRemoved = newItems.removeIfCompat {
            it.type == FormatDataType.BOLD &&
                    it.offset == selectionRange.start &&
                    it.offset + it.length == selectionRange.end
        }

        if (!wasRemoved) {
            newItems += VkMessage.FormatData.Item(
                offset = selectionRange.start,
                length = selectionRange.end - selectionRange.start,
                type = FormatDataType.BOLD,
                url = null
            )
        }

        formatData = formatData.copy(items = newItems)
        updateStyles()
    }

    override fun onItalicClicked() {
        val selectionRange = screenState.value.message.selection
        val newItems = formatData.items.toMutableList()
        val wasRemoved = newItems.removeIfCompat {
            it.type == FormatDataType.ITALIC &&
                    it.offset == selectionRange.start &&
                    it.offset + it.length == selectionRange.end
        }

        if (!wasRemoved) {
            newItems += VkMessage.FormatData.Item(
                offset = selectionRange.start,
                length = selectionRange.end - selectionRange.start,
                type = FormatDataType.ITALIC,
                url = null
            )
        }

        formatData = formatData.copy(items = newItems)
        updateStyles()
    }

    override fun onUnderlineClicked() {
        val selectionRange = screenState.value.message.selection
        val newItems = formatData.items.toMutableList()
        val wasRemoved = newItems.removeIfCompat {
            it.type == FormatDataType.UNDERLINE &&
                    it.offset == selectionRange.start &&
                    it.offset + it.length == selectionRange.end
        }

        if (!wasRemoved) {
            newItems += VkMessage.FormatData.Item(
                offset = selectionRange.start,
                length = selectionRange.end - selectionRange.start,
                type = FormatDataType.UNDERLINE,
                url = null
            )
        }

        formatData = formatData.copy(items = newItems)
        updateStyles()
    }

    override fun onLinkClicked() {

    }

    override fun onRegularClicked() {
        formatData = formatData.copy(items = emptyList())
        updateStyles()
    }

    override fun onReplyCloseClicked() {
        replyToCmId = null

        screenState.setValue { old ->
            old.copy(
                replyTitle = null,
                replyText = null
            )
        }
    }

    override fun onRequestReplyToMessage(cmId: Long) {
        replyToMessage(cmId)
    }

    override fun onKeyboardShown() {
        showKeyboard.setValue { false }
    }

    override suspend fun loadMessageReadPeers(peerId: Long, cmId: Long): Int =
        suspendCancellableCoroutine {
            viewModelScope.launch {
                getMessageReadPeersUseCase
                    .invoke(peerId = peerId, cmId = cmId)
                    .listenValue(viewModelScope) { state ->
                        state.processState(
                            error = { error ->
                                it.resume(-1)
                            },
                            success = { count ->
                                it.resume(count)
                            }
                        )
                    }
            }
        }

    private fun handleNewMessage(event: LongPollParsedEvent.MessageNew) {
        val message = event.message

        if (message.peerId != screenState.value.convoId) return
        if (messages.value.indexOfFirstOrNull { it.id == message.id } != null) return

        val randomIds = messages.value.map(VkMessage::randomId)
        if (message.randomId != 0L && message.randomId in randomIds) return

        val newMessages = messages.value.toMutableList()
        newMessages.add(0, message)

        messages.setValue { newMessages }

        syncUiMessages()
    }

    private fun handleEditedMessage(event: LongPollParsedEvent.MessageEdited) {
        val message = event.message
        if (message.peerId != screenState.value.convoId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.id == message.id }
        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            newMessages[index] = message
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleReadIncomingEvent(event: LongPollParsedEvent.IncomingMessageRead) {
        if (event.peerId != screenState.value.convoId) return

        val messages = messages.value
        val index = messages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // диалога нет в списке
            // pizdets
        } else {
            val newConvo = screenState.value.convo.copy(
                inReadCmId = event.cmId
            )

            screenState.setValue { old ->
                old.copy(convo = newConvo)
            }

            syncUiMessages()
        }
    }

    private fun handleReadOutgoingEvent(event: LongPollParsedEvent.OutgoingMessageRead) {
        if (event.peerId != screenState.value.convoId) return

        val messages = messages.value
        val index = messages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            val newConvo = screenState.value.convo.copy(
                outReadCmId = event.cmId
            )

            screenState.setValue { old ->
                old.copy(convo = newConvo)
            }

            syncUiMessages()
        }
    }

    private fun handleMessageDeleted(event: LongPollParsedEvent.MessageDeleted) {
        if (event.peerId != screenState.value.convoId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            newMessages.removeAt(index)
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleMessageRestored(event: LongPollParsedEvent.MessageRestored) {
        if (event.message.peerId != screenState.value.convoId) return

        val newMessages = messages.value.toMutableList()
        val minDate = newMessages.minOf(VkMessage::date)

        if (event.message.date < minDate) { // сообщения не должно быть в списке
            // pizdets
            return
        }

        newMessages.add(event.message)
        messages.setValue { newMessages.sorted() }
        syncUiMessages()
    }

    private fun handleMessageMarkedAsImportant(event: LongPollParsedEvent.MessageMarkedAsImportant) {
        if (event.peerId != screenState.value.convoId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            val newMessage = newMessages[index].copy(isImportant = event.marked)
            newMessages[index] = newMessage
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleMessageMarkedAsSpam(event: LongPollParsedEvent.MessageMarkedAsSpam) {
        if (event.peerId != screenState.value.convoId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            newMessages.removeAt(index)
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleMessageMarkedAsNotSpam(event: LongPollParsedEvent.MessageMarkedAsNotSpam) {
        if (event.message.peerId != screenState.value.convoId) return

        val newMessages = messages.value.toMutableList()
        val maxDate = newMessages.maxOf(VkMessage::date)
        val minDate = newMessages.minOf(VkMessage::date)

        if (event.message.date !in minDate..maxDate) return

        newMessages.add(event.message)
        messages.setValue { newMessages.sorted() }
        syncUiMessages()
    }

    private fun loadConvo() {
        loadConvosByIdUseCase(
            peerIds = listOf(screenState.value.convoId),
            extended = true,
            fields = VkConstants.ALL_FIELDS
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = { response ->
                    val convo = response.firstOrNull() ?: return@listenValue
                    val title = convo.extractTitle(
                        useContactName = AppSettings.General.useContactNames,
                        resources = resourceProvider.resources
                    )
                    val avatar = convo.extractAvatar()

                    screenState.setValue { old ->
                        old.copy(
                            convo = convo,
                            title = title,
                            avatar = avatar
                        )
                    }

                    convo.pinnedMessage?.let(::handlePinnedMessage)
                }
            )
        }
    }

    private fun handlePinnedMessage(pinnedMessage: VkMessage?) {
        if (pinnedMessage == null) {
            screenState.setValue { old ->
                old.copy(
                    pinnedMessage = null,
                    convo = old.convo.copy(
                        pinnedMessage = null,
                        pinnedMessageId = null
                    ),
                    pinnedSummary = null,
                    pinnedTitle = null
                )
            }
            return
        }

        val pinnedUser = VkMemoryCache.getUser(pinnedMessage.fromId)
        val pinnedGroup = VkMemoryCache.getGroup(abs(pinnedMessage.fromId))
        val pinnedTitle = pinnedUser?.fullName ?: pinnedGroup?.name

        val pinnedSummary = buildAnnotatedString {
            pinnedMessage.text?.let(::append) ?: append("...")
        }

        screenState.setValue { old ->
            old.copy(
                pinnedMessage = pinnedMessage,
                convo = old.convo.copy(
                    pinnedMessage = pinnedMessage,
                    pinnedMessageId = pinnedMessage.id
                ),
                pinnedSummary = pinnedSummary,
                pinnedTitle = pinnedTitle.orDots()
            )
        }
    }

    private fun loadMessagesHistory(offset: Int = currentOffset.value) {
        messagesUseCase.getMessagesHistory(
            convoId = screenState.value.convoId,
            count = MESSAGES_LOAD_COUNT,
            offset = offset,
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = { response ->
                    val messages = response.messages
                    val fullMessages = if (offset == 0) {
                        messages
                    } else {
                        this.messages.value.plus(messages)
                    }.sorted()

                    val convos = response.convos

                    imagesToPreload.setValue {
                        messages.mapNotNull { it.extractAvatar().extractUrl() }
                    }

                    messagesUseCase.storeMessages(messages)
                    convoUseCase.storeConvos(convos)

                    val itemsCountSufficient = messages.size == MESSAGES_LOAD_COUNT

                    val paginationExhausted = !itemsCountSufficient &&
                            this.messages.value.isNotEmpty()
                    screenState.setValue { old ->
                        old.copy(isPaginationExhausted = paginationExhausted)
                    }

                    this.messages.emit(fullMessages)
                    syncUiMessages()
                    canPaginate.setValue { itemsCountSufficient }
                }
            )

            screenState.setValue { old ->
                old.copy(
                    isLoading = offset == 0 && state.isLoading(),
                    isPaginating = offset > 0 && state.isLoading()
                )
            }
        }
    }

    private fun handleError(error: State.Error) {
        when (error) {
            is State.Error.ApiError -> {
                when (error.errorCode) {
                    VkErrorCode.USER_AUTHORIZATION_FAILED -> {
                        baseError.setValue { BaseError.SessionExpired }
                    }

                    else -> {
                        baseError.setValue {
                            BaseError.SimpleError(message = error.errorMessage)
                        }
                    }
                }
            }

            State.Error.ConnectionError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Connection error")
                }
            }

            State.Error.InternalError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Internal error")
                }
            }

            State.Error.UnknownError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Unknown error")
                }
            }

            else -> Unit
        }
    }

    private fun List<VkMessage>.sorted(): List<VkMessage> {
        return sortedWith { m1, m2 ->
            val dateDiff = m2.date - m1.date
            if (dateDiff != 0) {
                dateDiff
            } else {
                val idDiff = m2.id - m1.id
                idDiff.toInt()
            }
        }
    }

    private fun sendMessage(
        attachments: List<VkAttachment>? = null,
        stickerId: Long? = null
    ) {
        lastMessageText = if (stickerId != null) "" else screenState.value.message.text

        val newMessage = VkMessage(
            id = -1L - sendingMessages.size,
            cmId = -1L - sendingMessages.size,
            text = lastMessageText,
            isOut = true,
            peerId = screenState.value.convoId,
            fromId = UserConfig.userId,
            date = (System.currentTimeMillis() / 1000).toInt(),
            randomId = Random.nextInt().toLong(),
            action = null,
            actionMemberId = null,
            actionText = null,
            actionCmId = null,
            actionMessage = null,
            updateTime = null,
            isImportant = false,
            forwards = null,
            attachments = attachments,
            replyMessage = when {
                replyToCmId != null -> messages.value.find { it.cmId == replyToCmId }
                else -> null
            },
            geoType = null,
            user = VkMemoryCache.getUser(UserConfig.userId),
            group = null,
            actionUser = null,
            actionGroup = null,
            isPinned = false,
            isSpam = false,
            pinnedAt = null,
            formatData = formatData,
            isDeleted = false
        )
        formatData = formatData.copy(items = emptyList())
        sendingMessages += newMessage
        messages.setValue { old -> listOf(newMessage).plus(old) }
        syncUiMessages()

        screenState.setValue { old ->
            old.copy(
                message = TextFieldValue(),
                actionMode = ActionMode.RECORD_AUDIO,
                replyTitle = null,
                replyText = null
            )
        }

        val replyCmId = replyToCmId
        replyToCmId = null

        val forward = when {
            replyCmId != null -> {
                buildJsonObject {
                    put("peer_id", screenState.value.convoId)
                    put("conversation_message_ids", buildJsonArray { add(replyCmId) })
                    put("is_reply", true)
                }.toString()
            }

            else -> null
        }

        messagesUseCase.sendMessage(
            peerId = screenState.value.convoId,
            randomId = newMessage.randomId,
            message = newMessage.text,
            forward = forward,
            attachments = attachments,
            formatData = newMessage.formatData,
            stickerId = stickerId,
        ).listenValue(viewModelScope) { state ->
            state.processState(
                any = { sendingMessages.remove(newMessage) },
                error = { error ->
                    markMessageAsFailed(newMessage)
                },
                success = { response ->
                    updateSentMessage(newMessage, response.messageId, response.cmId)
                }
            )
        }
    }

    private fun markMessageAsFailed(newMessage: VkMessage) {
        val failedId = -500_000L - failedMessages.size
        val newFailedMessage = newMessage.copy(id = failedId)
        failedMessages += newFailedMessage

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOf(newMessage)
        if (index != -1) {
            newMessages[index] = newFailedMessage
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun updateSentMessage(
        old: VkMessage,
        messageId: Long,
        cmId: Long
    ) {
        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOf(old)
        if (index != -1) {
            newMessages[index] = old.copy(id = messageId, cmId = cmId)
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun startVoiceRecording() {
        if (!screenState.value.message.text.isBlank()) return
        if (voiceRecorder.isRecording.value) return

        val started = voiceRecorder.start()
        if (!started) {
            Toast.makeText(
                applicationContext,
                R.string.unknown_error_occurred,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cancelVoiceRecording() {
        voiceRecorder.cancel()
    }

    private fun sendVoiceRecording() {
        val durationSec = voiceRecorder.durationSec.value
        val recordedFile = voiceRecorder.stop() ?: return

        val placeholderAttachment = VkAudioMessageDomain(
            id = -1L,
            ownerId = UserConfig.userId,
            duration = durationSec,
            waveform = emptyList(),
            linkOgg = "",
            linkMp3 = "",
            accessKey = "",
            transcriptState = null,
            transcript = null
        )

        lastMessageText = ""

        val newMessage = VkMessage(
            id = -1L - sendingMessages.size,
            cmId = -1L - sendingMessages.size,
            text = "",
            isOut = true,
            peerId = screenState.value.convoId,
            fromId = UserConfig.userId,
            date = (System.currentTimeMillis() / 1000).toInt(),
            randomId = Random.nextInt().toLong(),
            action = null,
            actionMemberId = null,
            actionText = null,
            actionCmId = null,
            actionMessage = null,
            updateTime = null,
            isImportant = false,
            forwards = null,
            attachments = listOf(placeholderAttachment),
            replyMessage = when {
                replyToCmId != null -> messages.value.find { it.cmId == replyToCmId }
                else -> null
            },
            geoType = null,
            user = VkMemoryCache.getUser(UserConfig.userId),
            group = null,
            actionUser = null,
            actionGroup = null,
            isPinned = false,
            isSpam = false,
            pinnedAt = null,
            formatData = null,
            isDeleted = false
        )

        sendingMessages += newMessage
        messages.setValue { old -> listOf(newMessage).plus(old) }
        syncUiMessages()

        screenState.setValue { old ->
            old.copy(actionMode = ActionMode.RECORD_AUDIO)
        }

        val replyCmId = replyToCmId
        replyToCmId = null

        screenState.setValue { old ->
            old.copy(replyTitle = null, replyText = null)
        }

        val forward = when {
            replyCmId != null -> {
                buildJsonObject {
                    put("peer_id", screenState.value.convoId)
                    put("conversation_message_ids", buildJsonArray { add(replyCmId) })
                    put("is_reply", true)
                }.toString()
            }

            else -> null
        }

        viewModelScope.launch {
            val uploadedAttachment = withContext(Dispatchers.IO) {
                runCatching {
                    uploadVoiceMessage(peerId = screenState.value.convoId, file = recordedFile)
                }.onFailure { error ->
                    logger.error(this@MessagesHistoryViewModelImpl::class, "uploadVoiceMessage failed", error)
                }.getOrNull()
            }

            recordedFile.delete()

            val attachment = uploadedAttachment
            if (attachment == null) {
                sendingMessages.remove(newMessage)
                markMessageAsFailed(newMessage)
                return@launch
            }

            val messageWithRealAttachment = newMessage.copy(attachments = listOf(attachment))
            messages.setValue { list ->
                list.toMutableList().also { mutable ->
                    val index = mutable.indexOf(newMessage)
                    if (index != -1) {
                        mutable[index] = messageWithRealAttachment
                    }
                }
            }
            syncUiMessages()

            messagesUseCase.sendMessage(
                peerId = screenState.value.convoId,
                randomId = newMessage.randomId,
                message = "",
                forward = forward,
                attachments = listOf(attachment),
                formatData = null,
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    any = { sendingMessages.remove(messageWithRealAttachment) },
                    error = { _ ->
                        markMessageAsFailed(messageWithRealAttachment)
                    },
                    success = { response ->
                        updateSentMessage(
                            messageWithRealAttachment,
                            response.messageId,
                            response.cmId
                        )
                    }
                )
            }
        }
    }

    private suspend fun uploadVoiceMessage(
        peerId: Long,
        file: File
    ): VkAudioMessageDomain {
        logger.debug(this::class, "uploadVoiceMessage: start peerId=$peerId, file=${file.path}, size=${file.length()}")
        val uploadServerResponse = filesRepository
            .getMessagesUploadServer(
                peerId = peerId,
                type = FilesRepository.FileType.AUDIO_MESSAGE
            )
            .mapApiDefault()
            .success()

        val mimeType = if (file.name.endsWith(".ogg", ignoreCase = true)) "audio/ogg" else "audio/mp4"
        val requestBody = file.asRequestBody(mimeType.toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val uploadResponse = filesRepository
            .uploadFile(url = uploadServerResponse.uploadUrl, file = body)

        val uploadSuccess = uploadResponse.success()
        val savedFile = uploadSuccess.file ?: uploadSuccess.audioMessage
            ?: error("Uploaded file is null: error=${uploadSuccess.error}")

        val saveResponse = filesRepository
            .saveMessageFile(savedFile)
            .mapApiDefault()
            .success()

        val voiceMessage = saveResponse.voiceMessage
            ?: saveResponse.file?.preview?.audioMessage
            ?: error("Neither voiceMessage nor file.preview.audioMessage in save response: $saveResponse")

        return voiceMessage.toDomain()
    }

    override fun onVoiceMessageClicked(attachment: VkAudioMessageDomain) {
        val url = attachment.linkMp3.ifBlank { attachment.linkOgg }
        if (url.isBlank()) return

        voicePlayer.toggle(url)
    }

    private fun confirmDeleteCurrentEditMessage() {
        val currentMessage = editMessage ?: return

        this.dialog.setValue {
            MessageDialog.MessageDelete(currentMessage)
        }
    }

    private fun editCurrentEditMessage() {
        replyToCmId = null

        val newText = screenState.value.message.text

        val lastText = lastMessageText.orEmpty().trim()

        screenState.setValue { old ->
            old.copy(
                editCmId = null,
                message = TextFieldValue(
                    text = lastText,
                    selection = TextRange(lastText.length)
                ),
                actionMode = if (lastText.isBlank()) ActionMode.RECORD_AUDIO
                else ActionMode.SEND,

                // TODO: 13/03/2026, Danil Nikolaev: save last reply
                replyTitle = null,
                replyText = null
            )
        }

        syncUiMessages()

        // TODO: 13/03/2026, Danil Nikolaev: actually edit message

        val newMessage = editMessage?.copy(
            replyMessage = if (replyToCmId == null) null else editMessage?.replyMessage,
            text = newText
        ) ?: return

        // TODO: 13/03/2026, Danil Nikolaev: check if message is exact same, then do not edit
    }

    private fun markAsImportant(
        messageIds: List<Long>,
        important: Boolean,
    ) {
        messagesUseCase.markAsImportant(
            peerId = screenState.value.convoId,
            messageIds = messageIds,
            important = important
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = {
                    val newMessages = messages.value
                        .toMutableList()
                        .map { message ->
                            if (message.id in messageIds) {
                                message.copy(isImportant = important)
                            } else {
                                message
                            }
                        }
                    messages.setValue { newMessages }
                    syncUiMessages()
                }
            )
        }
    }

    private fun deleteMessage(
        messageIds: List<Long>,
        spam: Boolean = false,
        deleteForAll: Boolean = false,
        onSuccess: () -> Unit = {}
    ) {
        messagesUseCase.delete(
            peerId = screenState.value.convoId,
            messageIds = messageIds,
            spam = spam,
            deleteForAll = deleteForAll
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = {
                    onSuccess()
                    val newMessages = messages.value.toMutableList()
                    val messagesToDelete = newMessages.filter { it.id in messageIds }
                    newMessages.removeAll(messagesToDelete)
                    messages.setValue { newMessages }
                    syncUiMessages()
                }
            )
        }
    }

    private fun pinMessage(messageId: Long) {
        messagesUseCase.pin(
            peerId = screenState.value.convoId,
            messageId = messageId,
            cmId = null
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = { pinnedMessage ->
                    handlePinnedMessage(pinnedMessage)

                    val newMessages = messages.value.toMutableList()
                    val index = newMessages.indexOfFirstOrNull { it.id == messageId }

                    if (index == null) {// сообщения нет в списке
                        // pizdets
                    } else {
                        newMessages[index] = pinnedMessage
                        messages.setValue { newMessages }
                        syncUiMessages()
                    }
                }
            )
        }
    }

    private fun unpinMessage(messageId: Long) {
        messagesUseCase.unpin(screenState.value.convoId)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = {
                        val newMessages = messages.value.toMutableList()
                        val index = newMessages.indexOfFirstOrNull { it.id == messageId }

                        if (index == null) { // сообщения нет в списке
                            // pizdets
                        } else {
                            newMessages[index] = newMessages[index].copy(isPinned = false)
                            messages.setValue { newMessages }
                            syncUiMessages()
                        }

                        handlePinnedMessage(null)
                    }
                )
            }
    }

    private fun readMessage(message: VkMessage) {
        messagesUseCase.markAsRead(
            peerId = screenState.value.convoId,
            startMessageId = message.id
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = {
                    val oldConvo = screenState.value.convo
                    val newConvo = oldConvo.copy(
                        inRead =
                            if (!message.isOut) message.id
                            else oldConvo.inRead,
                        outRead =
                            if (message.isOut) message.id
                            else oldConvo.outRead
                    )

                    screenState.setValue { old ->
                        old.copy(convo = newConvo)
                    }

                    syncUiMessages()
                }
            )
        }
    }

    private var lastMarkedReadCmId: Long = 0L
    private var markAsReadJob: Job? = null

    override fun onMessageSeen(messageId: Long, cmId: Long) {
        if (cmId <= 0 || messageId <= 0) return
        val currentInReadCmId = screenState.value.convo.inReadCmId
        if (cmId <= currentInReadCmId || cmId <= lastMarkedReadCmId) return

        lastMarkedReadCmId = cmId

        val oldConvo = screenState.value.convo
        val newConvo = oldConvo.copy(
            inReadCmId = maxOf(currentInReadCmId, cmId),
            inRead = maxOf(oldConvo.inRead ?: 0L, messageId)
        )

        screenState.setValue { old ->
            old.copy(convo = newConvo)
        }

        syncUiMessages()

        markAsReadJob?.cancel()
        markAsReadJob = viewModelScope.launch {
            delay(350L)
            messagesUseCase.markAsRead(
                peerId = screenState.value.convoId,
                startMessageId = messageId
            ).listenValue(this) { state ->
                state.processState(
                    error = { _ -> },
                    success = {
                        // Mark as read acknowledged on VK API
                    }
                )
            }
        }
    }

    private fun copyMessage(message: VkMessage) {
        val clipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val messageToCopy = message.text.orEmpty().trim()
        if (messageToCopy.isEmpty()) {
            val photo = with(message.attachments.orEmpty()) {
                if (size == 1 && all { it is VkPhotoDomain }) {
                    first() as? VkPhotoDomain
                } else null
            } ?: return

            val photoMaxSize = photo.getMaxSize() ?: return

            viewModelScope.launch(Dispatchers.IO) {
                val drawable = applicationContext.imageLoader.execute(
                    ImageRequest.Builder(applicationContext)
                        .data(photoMaxSize.url)
                        .build()
                ).drawable ?: return@launch

                val imagesDir = File(applicationContext.cacheDir, "images")
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val imageFile = File(imagesDir, "shared_image_id${photo.id}.png")
                FileOutputStream(imageFile).use {
                    drawable.toBitmapOrNull()?.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}.provider",
                    imageFile
                )

                val clip = ClipData.newUri(applicationContext.contentResolver, "Image", uri)
                clipboardManager.setPrimaryClip(clip)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Image copied to clipboard",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return
        }

        clipboardManager.setPrimaryClip(ClipData.newPlainText("Message", messageToCopy))

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            Toast.makeText(applicationContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun syncUiMessages(): List<MessageUiItem> {
        val messages = messages.value
        val selectedMessages = selectedMessages.value

        val newUiMessages = messages.mapIndexed { index, message ->
            message.asPresentation(
                resourceProvider = resourceProvider,
                showName = true,
                prevMessage = messages.getOrNull(index + 1),
                nextMessage = messages.getOrNull(index - 1),
                showTimeInActionMessages = AppSettings.Experimental.showTimeInActionMessages,
                convo = screenState.value.convo,
                isSelected = screenState.value.editCmId == message.cmId ||
                        selectedMessages.indexOfFirstOrNull { it.id == message.id } != null
            )
        }
        uiMessages.setValue { newUiMessages }

        return newUiMessages
    }

    override fun onCleared() {
        ActiveChatTracker.onChatClosed(screenState.value.convoId)
        voicePlayer.release()
        voiceRecorder.release()
        super.onCleared()
    }

    companion object {
        const val MESSAGES_LOAD_COUNT = 30
    }
}


// TODO: 25.08.2023, Danil Nikolaev: this and down below - rewrite

//    suspend fun uploadPhoto(
//        peerid: Long,
//        photo: File,
//        name: String,
//    ) {
//        suspendCoroutine {
//            viewModelScope.launch {
//                val uploadServerUrl = getPhotoMessageUploadServer(peerId)
//                val uploadedFileInfo = uploadPhotoToServer(uploadServerUrl, photo, name)
//
//                val savedAttachment = saveMessagePhoto(
//                    uploadedFileInfo.first,
//                    uploadedFileInfo.second,
//                    uploadedFileInfo.third
//                )
//
//                it.resume(savedAttachment)
//            }
//        }
//    }

//    private suspend fun getPhotoMessageUploadServer(peerid: Long) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { photosRepository.getMessagesUploadServer(peerId) }
//                ).response?.let { response ->
//                    continuation.resume(response.uploadUrl)
//                }
//            }
//        }
//    }

//    private suspend fun uploadPhotoToServer(
//        uploadUrl: String,
//        photo: File,
//        name: String,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val requestBody = photo.asRequestBody("image/*".toMediaType())
//                val body = MultipartBody.Part.createFormData("photo", name, requestBody)

//            sendRequestNotNull(
//                onError = { exception ->
//                    continuation.resumeWithException(exception)
//                    true
//                },
//                request = { photosRepository.uploadPhoto(uploadUrl, body) }
//            ).let { response ->
//                continuation.resume(Triple(response.server, response.photo, response.hash))
//            }
//            }
//        }
//    }

//    private suspend fun saveMessagePhoto(
//        server: Int,
//        photo: String,
//        hash: String,
//    ) = suspendCoroutine<VkAttachment> { continuation ->
//        viewModelScope.launch {
//            sendRequestNotNull(
//                onError = { exception ->
//                    continuation.resumeWithException(exception)
//                    true
//                },
//                request = {
//                    photosRepository.saveMessagePhoto(
//                        PhotosSaveMessagePhotoRequest(photo, server, hash)
//                    )
//                }
//            ).response?.first()?.toDomain()?.let(continuation::resume)
//        }
//    }

//    suspend fun uploadVideo(
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine {
//            viewModelScope.launch {
//                val uploadInfo = getVideoMessageUploadServer()
//
//                uploadVideoToServer(
//                    uploadInfo.first,
//                    file,
//                    name
//                )
//
//                it.resume(uploadInfo.second)
//            }
//        }
//    }

//    private suspend fun getVideoMessageUploadServer() {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//            sendRequestNotNull(
//                onError = { exception ->
//                    continuation.resumeWithException(exception)
//                    true
//                },
//                request = { videosRepository.save() }
//            ).response?.let { response ->
//                val uploadUrl = response.uploadUrl
//                val video = VkVideoDomain(
//                    id = response.videoId,
//                    ownerId = response.ownerId,
//                    images = emptyList(),
//                    firstFrames = null,
//                    accessKey = response.accessKey,
//                    title = response.title
//                )
//
//                continuation.resume(uploadUrl to video)
//            }
//            }
//        }
//    }

//    private suspend fun uploadVideoToServer(
//        uploadUrl: String,
//        file: File,
//        name: String,
//    ) {
//        viewModelScope.launch {
//            val requestBody = file.asRequestBody()
//            val body = MultipartBody.Part.createFormData("video_file", name, requestBody)
//
//            sendRequest(
//                onError = { exception -> throw exception },
//                request = { videosRepository.upload(uploadUrl, body) }
//            )
//        }
//    }

//    suspend fun uploadAudio(
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine {
//            viewModelScope.launch {
//                val uploadUrl = getAudioUploadServer()
//                val uploadInfo = uploadAudioToServer(uploadUrl, file, name)
//                val saveInfo = saveMessageAudio(
//                    uploadInfo.first, uploadInfo.second, uploadInfo.third
//                )
//
//                it.resume(saveInfo)
//            }
//        }
//    }

//    private suspend fun getAudioUploadServer() {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { audiosRepository.getUploadServer() }
//                ).response?.uploadUrl?.let(continuation::resume)
//            }
//        }
//    }

//    private suspend fun uploadAudioToServer(
//        uploadUrl: String,
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val requestBody = file.asRequestBody()
//                val body = MultipartBody.Part.createFormData("file", name, requestBody)
//
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { audiosRepository.upload(uploadUrl, body) }
//                ).let { response ->
//                    response.error?.let { error -> throw ApiException(error = error) }
//
//                    continuation.resume(
//                        Triple(response.server, response.audio.notNull(), response.hash)
//                    )
//                }
//            }
//        }
//    }

//    private suspend fun saveMessageAudio(
//        server: Int,
//        audio: String,
//        hash: String,
//    ) {
//        suspendCoroutine<VkAttachment> { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { audiosRepository.save(server, audio, hash) }
//                ).response?.toDomain()?.let(continuation::resume)
//            }
//        }
//    }

//    suspend fun uploadFile(
//        peerid: Long,
//        file: File,
//        name: String,
//        type: FilesRepository.FileType,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val uploadServerUrl = getFileMessageUploadServer(peerId, type)
//                val uploadedFileInfo = uploadFileToServer(uploadServerUrl, file, name)
//                val savedAttachmentPair = saveMessageFile(uploadedFileInfo)
//
//                continuation.resume(savedAttachmentPair.second)
//            }
//        }
//    }

//    private suspend fun getFileMessageUploadServer(
//        peerid: Long,
//        type: FilesRepository.FileType,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val uploadServerResponse = sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { filesRepository.getMessagesUploadServer(peerId, type) }
//                ).response.notNull()
//
//                continuation.resume(uploadServerResponse.uploadUrl)
//            }
//        }
//    }

//    private suspend fun uploadFileToServer(
//        uploadUrl: String,
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val requestBody = file.asRequestBody()
//                val body = MultipartBody.Part.createFormData("file", name, requestBody)
//
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { filesRepository.uploadFile(uploadUrl, body) }
//                ).let { response ->
//                    response.error?.let { error -> throw ApiException(error = error) }
//
//                    continuation.resume(response.file.notNull())
//                }
//            }
//        }
//    }

//    private suspend fun saveMessageFile(file: String) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { filesRepository.saveMessageFile(file) }
//                ).response?.let { response ->
//                    val type = response.type
//                    val attachmentFile =
//                        response.file?.toDomain() ?: response.voiceMessage?.toDomain()
//
//                    continuation.resume(type to attachmentFile.notNull())
//                }
//            }
//        }
//    }
//}

//data class MessagesLoadedEvent(
//    val count: Int,
//    val conversations: HashMap<Int, VkConversationDomain>,
//    val messages: List<VkMessageDomain>,
//    val profiles: HashMap<Int, VkUserDomain>,
//    val groups: HashMap<Int, VkGroupDomain>,
//) : VkEvent()
//
//data class MessagesMarkAsImportantEvent(val messagesIds: List<Int>, val important: Boolean) :
//    VkEvent()
//
//data class MessagesPinEvent(val message: VkMessageDomain) : VkEvent()
//
//object MessagesUnpinEvent : VkEvent()
//
//data class MessagesDeleteEvent(val peerid: Long, val messagesIds: List<Int>) : VkEvent()
//
//data class MessagesEditEvent(val message: VkMessageDomain) : VkEvent()
//
//data class MessagesReadEvent(
//    val isOut: Boolean,
//    val peerid: Long,
//    val messageid: Long,
//) : VkEvent()
//
//data class MessagesNewEvent(
//    val message: VkMessageDomain,
//    val profiles: HashMap<Int, VkUserDomain>,
//    val groups: HashMap<Int, VkGroupDomain>,
//) : VkEvent()
