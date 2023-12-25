package com.meloda.fast.screens.updates

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.meloda.fast.R
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.AppConstants
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.common.UpdateManagerState
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.updateValue
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.model.base.UiText
import com.meloda.fast.receiver.DownloadManagerReceiver
import com.meloda.fast.screens.updates.model.UpdateState
import com.meloda.fast.screens.updates.model.UpdatesScreenState
import com.meloda.fast.util.AndroidUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

interface UpdatesViewModel {
    val screenState: MutableStateFlow<UpdatesScreenState>

    val isNeedToShowChangelogAlert: Flow<Boolean>
    val isNeedToShowUnknownSourcesAlert: Flow<Boolean>
    val isNeedToShowIssuesAlert: Flow<Boolean>
    val isNeedToShowFileNotFoundAlert: Flow<Boolean>

    fun onUpdateItemExists(updateItem: UpdateItem)

    fun checkUpdates()

    fun onChangelogButtonClicked()
    fun onActionButtonClicked()
    fun onCancelDownloadButtonClicked()
    fun onIssuesButtonClicked()

    fun onChangelogAlertDismissed()
    fun onUnknownSourcesAlertDismissed()
    fun onIssuesAlertDismissed()
    fun onIssuesAlertPositiveButtonClicked()
    fun onFileNotFoundAlertDismissed()
}

class UpdatesViewModelImpl constructor(
    private val updateManager: UpdateManager,
) : BaseViewModel(), UpdatesViewModel {

    override val screenState = MutableStateFlow(UpdatesScreenState.EMPTY)

    override val isNeedToShowChangelogAlert = MutableStateFlow(false)
    override val isNeedToShowUnknownSourcesAlert = MutableStateFlow(false)
    override val isNeedToShowIssuesAlert = MutableStateFlow(false)
    override val isNeedToShowFileNotFoundAlert = MutableStateFlow(false)

    private var currentJob: Job? = null

    private val downloadManager by lazy {
        AppGlobal.Instance.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    init {
        updateManager.stateFlow.listenValue(::updateState)

        checkUpdates()
    }

    override fun onUpdateItemExists(updateItem: UpdateItem) {
        val newForm = screenState.value.copy(
            updateItem = updateItem,
            updateState = UpdateState.NewUpdate,
            error = null
        )
        screenState.update { newForm }
    }

    override fun checkUpdates() {
        if (currentJob != null) {
            currentJob?.cancel()
            currentJob = null
        }

        updateUpdateState(UpdateState.Loading)

        currentJob = updateManager.checkUpdates().apply {
            invokeOnCompletion { currentJob = null }
        }
    }

    override fun onChangelogButtonClicked() {
        isNeedToShowChangelogAlert.tryEmit(true)
    }

    override fun onActionButtonClicked() {
        val state = screenState.value.updateState

        if (!state.isDownloaded()) {
            downloadUpdate()
            return
        }

        when (state) {
            UpdateState.NewUpdate -> checkIsInstallingAllowed()
            UpdateState.NoUpdates, UpdateState.Error -> checkUpdates()
            UpdateState.Downloaded -> installUpdate()
            else -> Unit
        }
    }

    override fun onCancelDownloadButtonClicked() {
        when (screenState.value.updateState) {
            UpdateState.Downloading -> cancelCurrentDownload()
            else -> Unit
        }
    }

    override fun onIssuesButtonClicked() {
        isNeedToShowIssuesAlert.tryEmit(true)
    }

    override fun onChangelogAlertDismissed() {
        isNeedToShowChangelogAlert.tryEmit(false)
    }

    override fun onUnknownSourcesAlertDismissed() {
        isNeedToShowUnknownSourcesAlert.tryEmit(false)
    }

    override fun onIssuesAlertDismissed() {
        isNeedToShowIssuesAlert.tryEmit(false)
    }

    override fun onIssuesAlertPositiveButtonClicked() {
        deleteInstalledFile()
        checkUpdates()
    }

    override fun onFileNotFoundAlertDismissed() {
        isNeedToShowFileNotFoundAlert.tryEmit(false)
        checkUpdates()
    }

    private fun deleteInstalledFile() {
        // TODO: 26.03.2023, Danil Nikolaev: use updateItem
        val apkName = "bruhLol"

        val apkFileName = "$apkName.apk"

        val destination = "%s/$apkFileName".format(
            AppGlobal.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        )

        val file = File(destination)
        if (!file.exists()) return
        file.delete()
    }

    // TODO: 21/11/2023, Danil Nikolaev: check if still works
    private fun updateState(updateManagerState: UpdateManagerState) {
        val item = updateManagerState.updateItem
        val error = updateManagerState.throwable

        var fileExists = false

        if (item != null) {
            val apkName = item.fileName

            val apkFileName = "$apkName.apk"

            val destination = "%s/$apkFileName".format(
                AppGlobal.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
            )

            val file = File(destination)
            fileExists = file.exists()
        }

        val newUpdateState = when {
            item != null -> {
                if (fileExists) {
                    UpdateState.Downloaded
                } else {
                    UpdateState.NewUpdate
                }
            }

            error != null -> UpdateState.Error
            else -> UpdateState.NoUpdates
        }
        updateUpdateState(newUpdateState)

        val newError = error?.message

        val newState = screenState.value.copy(
            updateItem = item,
            error = newError
        )
        screenState.update { newState }
    }

    private fun checkIsInstallingAllowed() {
        if (!isSdkAtLeast(Build.VERSION_CODES.O) && !AndroidUtils.isCanInstallUnknownApps()) {
            isNeedToShowUnknownSourcesAlert.update { true }
        } else {
            downloadUpdate()
        }
    }

    private var downloadId: Long? = null

    private fun downloadUpdate() {
        val context = AppGlobal.Instance

        updateUpdateState(UpdateState.Loading)
//        val newUpdate = screenState.value.updateItem ?: return

        // TODO: 26.03.2023, Danil Nikolaev: use updateItem
        val apkName = "bruhLol"

        val apkFileName = "$apkName.apk"

        val destination = "%s/$apkFileName".format(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        )

        val file = File(destination)
        if (file.exists()) {
            updateUpdateState(UpdateState.Downloaded)
            return
        }

        val downloadUri = try {
            Uri.parse(
                "https://vk.com/doc157582555_635903147?hash=gTEOVno21WCtxX9GclYo8Liloat5V4xt4WB6nSuOMl8&dl=PQvcF2f7jyJDhJzMFOfRzCZXMx0MztmnwzhQYe4Ycdz"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Uri.EMPTY
        }

        val request = DownloadManager.Request(downloadUri).apply {
            setTitle("${context.getString(R.string.app_name)} $apkFileName")
            setMimeType(AppConstants.INSTALL_APP_MIME_TYPE)
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                apkFileName
            )
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or
                        DownloadManager.Request.NETWORK_MOBILE
            )
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val receiver = DownloadManagerReceiver()
        receiver.onReceiveAction = {
            downloadId = null

            installUpdate(file)

            context.unregisterReceiver(receiver)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        downloadId = downloadManager.enqueue(request)

        updateUpdateState(UpdateState.Downloading)

        var isDownloaded = false

        createTimerFlow(
            isNeedToEndCondition = { isDownloaded },
            onStartAction = {
                Log.d("Downloading update", "downloadUpdate: onStart")
            },
            onTickAction = {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId ?: -1)

                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val sizeIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val downloadedIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val size = cursor.getInt(sizeIndex)
                    val downloaded = cursor.getInt(downloadedIndex)
                    val progress = if (size != -1) {
                        downloaded * 100.0F / size
                    } else {
                        0.0F
                    }

                    val intProgress = progress.roundToInt()
                    if (intProgress >= 1) {
                        screenState.updateValue(
                            screenState.value.copy(currentDownloadProgress = intProgress)
                        )
                    }

                    Log.d("Downloading update", "progress: $progress%")

                    if (intProgress >= 100) {
                        isDownloaded = true
                        screenState.updateValue(
                            screenState.value.copy(currentDownloadProgress = 0)
                        )
                        updateUpdateState(UpdateState.Downloaded)
                    }
                }
            },
            onEndAction = {},
            interval = 250.milliseconds
        ).launchIn(viewModelScope)
    }

    private fun checkDownloadedFileExists(): File? {
        // TODO: 26.03.2023, Danil Nikolaev: use updateItem
        val apkName = "bruhLol"

        val apkFileName = "$apkName.apk"

        val destination = "%s/$apkFileName".format(
            AppGlobal.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        )

        val file = File(destination)
        return if (file.exists()) file else null
    }

    private fun installUpdate(file: File? = null) {
        val context = AppGlobal.Instance
        val destinationFile = file ?: checkDownloadedFileExists() ?: run {
            isNeedToShowFileNotFoundAlert.tryEmit(true)
            return
        }

        val installIntent = AndroidUtils.getInstallPackageIntent(
            context,
            ARG_PROVIDER_PATH,
            destinationFile
        )

        context.startActivity(installIntent)
    }

    private fun updateUpdateState(newState: UpdateState) {
        val title = when (newState) {
            UpdateState.Error -> R.string.error_occurred
            UpdateState.NewUpdate -> R.string.fragment_updates_new_version
            UpdateState.NoUpdates -> R.string.fragment_updates_no_updates
            UpdateState.Downloaded -> R.string.fragment_updates_downloaded
            else -> null
        }?.let(UiText::Resource)

        val subtitle = when (newState) {
            UpdateState.Error -> {
                screenState.value.error?.let { error ->
                    if (error.contains("cannot be converted", ignoreCase = true)
                        || error.contains("begin_object", ignoreCase = true)
                    ) {
                        UiText.Simple("OTA Server is unavailable")
                    } else {
                        UiText.ResourceParams(
                            R.string.error_occurred_description,
                            listOf(error)
                        )
                    }
                }
            }

            UpdateState.NewUpdate, UpdateState.Downloaded -> {
                screenState.value.updateItem?.let { item ->
                    UiText.ResourceParams(
                        R.string.fragment_updates_new_version_description,
                        listOf(item.versionName)
                    )
                }
            }

            UpdateState.NoUpdates -> {
                UiText.Resource(R.string.fragment_updates_no_updates_description)
            }

            else -> null
        }

        val actionButtonText = when (newState) {
            UpdateState.Error -> R.string.fragment_updates_try_again
            UpdateState.NewUpdate -> R.string.fragment_updates_download_update
            UpdateState.NoUpdates -> R.string.fragment_updates_check_updates
            UpdateState.Downloaded -> R.string.fragment_updates_install
            else -> null
        }?.let(UiText::Resource)

        val actionButtonIcon = when (newState) {
            UpdateState.Error -> R.drawable.round_restart_alt_24
            UpdateState.NewUpdate -> R.drawable.round_file_download_24
            UpdateState.Downloaded -> R.drawable.round_install_mobile_24
            else -> null
        }

        screenState.updateValue(
            screenState.value.copy(
                updateState = newState,
                title = title,
                subtitle = subtitle,
                actionButtonText = actionButtonText,
                actionButtonIcon = actionButtonIcon
            )
        )
    }

    private fun cancelCurrentDownload() {
        screenState.updateValue(
            screenState.value.copy(currentDownloadProgress = 0)
        )
        downloadId?.run { downloadManager.remove(this) }
        checkUpdates()
    }

    companion object {
        private const val ARG_PROVIDER_PATH = ".provider"
    }
}


/*
   private fun showUnknownSourcesAlert() {
        context?.showDialog(
            title = UiText.Resource(R.string.warning),
            message = UiText.Resource(R.string.fragment_updates_unknown_sources_disabled_message),
            positiveText = UiText.Resource(R.string.yes),
            positiveAction = { AndroidUtils.openInstallUnknownAppsScreen(requireContext()) },
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onUnknownSourcesAlertDismissed,
            isCancelable = false
        )
    }

    private fun showChangelogAlert() {
        val messageText =
            viewModel.screenState.value.updateItem?.changelog?.ifBlank {
                changelogPlaceholder
            } ?: changelogPlaceholder

        context?.showDialog(
            title = UiText.Resource(R.string.fragment_updates_changelog),
            message = UiText.Simple(messageText),
            positiveText = UiText.Resource(R.string.ok),
            onDismissAction = viewModel::onChangelogAlertDismissed
        )
    }

    private fun showIssuesAlert() {
        context?.showDialog(
            message = UiText.Resource(R.string.fragment_updates_issues_description),
            positiveText = UiText.Resource(R.string.action_delete),
            positiveAction = viewModel::onIssuesAlertPositiveButtonClicked,
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onIssuesAlertDismissed
        )
    }

    private fun showFileNotFoundAlert() {
        context?.showDialog(
            title = UiText.Resource(R.string.warning),
            message = UiText.Resource(R.string.fragment_updates_file_not_found_description),
            positiveText = UiText.Resource(R.string.ok),
            onDismissAction = viewModel::onFileNotFoundAlertDismissed,
            isCancelable = false
        )
    }

    private fun writeFileToStorage(responseBody: ResponseBody?) {
        if (responseBody == null) return

        val updateItem = requireNotNull(viewModel.currentItem.value)

        try {
            val destination = requireContext()
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() +
                    "${File.separator}${updateItem.fileName}.${updateItem.extension}"

            val file = File(destination)
            if (file.exists()) file.delete()

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize: Long = responseBody.contentLength()

                requireActivity().runOnUiThread {
                    binding.loadingProgress.max = fileSize.toInt()
                    binding.loadingProgress.progress = 0
                }

                var fileSizeDownloaded: Long = 0
                inputStream = responseBody.byteStream()
                outputStream = FileOutputStream(file)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                outputStream.flush()

                requireActivity().runOnUiThread {
                    installUpdate(file)
                }
            } catch (e: IOException) {

            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {

        }
    }
 */
